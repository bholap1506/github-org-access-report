# GitHub Organization Access Report Service

A REST API service that connects to GitHub and generates a report showing which users have access to which repositories within a given organization. This service is designed to scale efficiently for organizations with 100+ repositories and 1000+ users.

## Features

- **GitHub Authentication**: Secure authentication using GitHub Personal Access Token or OAuth
- **Repository Listing**: Retrieve all repositories in an organization
- **User Access Mapping**: Identify which users have access to each repository (direct members + collaborators)
- **Scalable Architecture**: Optimized for large organizations with concurrent API calls
- **Comprehensive Report**: JSON API endpoint returning structured access data
- **Rate Limit Management**: Handles GitHub API rate limiting gracefully
- **Error Handling**: Robust error handling with meaningful error messages

## Architecture

```
┌─────────────────────────────────────────┐
│   Spring Boot REST API Controller       │
│   /api/v1/org/{org}/access-report      │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Access Report Service                 │
│   - Orchestrates data retrieval         │
│   - Aggregates results                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   GitHub Service                        │
│   - Repo Service                        │
│   - User Service                        │
│   - Collaborator Service                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   GitHub API Client (RestTemplate)      │
│   - REST API calls                      │
│   - GraphQL queries (optional)          │
└─────────────────────────────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- GitHub Personal Access Token with `repo`, `read:user`, `read:org` scopes

## Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/bholap1506/github-org-access-report.git
cd github-org-access-report
```

### 2. Configure GitHub Authentication

Create a `.env` file in the root directory:

```env
GITHUB_TOKEN=your_personal_access_token_here
GITHUB_API_URL=https://api.github.com
```

Or set environment variables:

```bash
export GITHUB_TOKEN=your_personal_access_token_here
```

**How to create a GitHub Personal Access Token:**
1. Go to GitHub Settings > Developer settings > Personal access tokens
2. Click "Generate new token"
3. Select scopes: `repo`, `read:user`, `read:org`
4. Copy the token and store it securely

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Get Organization Access Report

**Endpoint**: `GET /api/v1/org/{org}/access-report`

**Parameters**:
- `org` (path parameter): GitHub organization name

**Example Request**:
```bash
curl -X GET http://localhost:8080/api/v1/org/kubernetes/access-report
```

**Response** (200 OK):
```json
{
  "organization": "kubernetes",
  "totalRepositories": 150,
  "totalUsers": 856,
  "generatedAt": "2024-03-14T15:30:00Z",
  "users": [
    {
      "username": "john-doe",
      "name": "John Doe",
      "role": "admin",
      "repositories": [
        {
          "name": "kubernetes",
          "fullName": "kubernetes/kubernetes",
          "permission": "admin",
          "isPrivate": false
        },
        {
          "name": "enhancements",
          "fullName": "kubernetes/enhancements",
          "permission": "push",
          "isPrivate": false
        }
      ],
      "repositoryCount": 2
    },
    {
      "username": "jane-smith",
      "name": "Jane Smith",
      "role": "member",
      "repositories": [
        {
          "name": "kubernetes",
          "fullName": "kubernetes/kubernetes",
          "permission": "pull",
          "isPrivate": false
        }
      ],
      "repositoryCount": 1
    }
  ]
}
```

**Error Response** (401 Unauthorized):
```json
{
  "error": "Invalid or missing GitHub token",
  "timestamp": "2024-03-14T15:30:00Z"
}
```

**Error Response** (404 Not Found):
```json
{
  "error": "Organization 'invalid-org' not found",
  "timestamp": "2024-03-14T15:30:00Z"
}
```

## Design Decisions & Assumptions

### 1. **Authentication Method**
- Uses GitHub Personal Access Token for authentication
- Can be extended to support OAuth 2.0 for user-delegated access
- Token is read from environment variable or .env file

### 2. **Scalability Approach**
- **Concurrent API Calls**: Uses CompletableFuture and ForkJoinPool for parallel repository and user fetching
- **Pagination**: Implements GitHub API pagination (per_page=100) to minimize requests
- **Caching** (Optional): Spring Cache abstraction for caching results (5-minute TTL)
- **Rate Limit Handling**: Tracks remaining rate limit and returns error if exhausted

### 3. **Data Aggregation**
- Fetches all repositories first
- For each repository, fetches collaborators (includes organization members)
- Aggregates results in-memory
- Returns flattened user → repositories mapping

### 4. **API Response Structure**
- User-centric view: Primary grouping is by user
- Includes both direct members and repository collaborators
- Permission levels: admin, push, pull, maintain, triage

### 5. **Error Handling**
- Validates GitHub token before processing
- Gracefully handles API timeouts
- Returns meaningful error messages
- Logs detailed errors for debugging

### 6. **Assumptions**
- GitHub organization is publicly visible or token has access
- Collaborators are returned for each repository (requires adequate permissions)
- Private repositories are included if token has access
- Organization members are automatically included in results
- Caching can be disabled via configuration if real-time data is required

## Project Structure

```
src/main/java/com/github/accessreport/
├── controller/
│   └── AccessReportController.java
├── service/
│   ├── AccessReportService.java
│   ├── GitHubService.java
│   ├── RepositoryService.java
│   └── UserService.java
├── client/
│   └── GitHubRestClient.java
├── model/
│   ├── dto/
│   │   ├── AccessReportDTO.java
│   │   ├── RepositoryDTO.java
│   │   └── UserAccessDTO.java
│   └── entity/
│       ├── GitHubUser.java
│       └── GitHubRepository.java
├── config/
│   └── GitHubConfig.java
├── exception/
│   └── GitHubServiceException.java
└── GitHubAccessReportApplication.java
```

## Configuration

### application.properties

```properties
# GitHub Configuration
github.token=${GITHUB_TOKEN}
github.api.url=https://api.github.com
github.api.timeout=30s

# Caching
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=5m

# Logging
logging.level.com.github.accessreport=INFO
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## Performance Considerations

1. **Rate Limiting**: GitHub API allows 5000 requests/hour per token
   - Fetching 100 repos with collaborators ≈ 200 API calls
   - Sufficient for multiple organizations

2. **Response Time**:
   - Small org (10 repos, 50 users): ~2-3 seconds
   - Large org (100 repos, 1000 users): ~15-30 seconds
   - Can be improved with caching and pagination

3. **Memory**:
   - Stores all data in-memory before response
   - Suitable for organizations up to 10K users
   - For larger datasets, consider streaming response

## Future Enhancements

- [ ] GraphQL API support (more efficient than REST)
- [ ] Streaming JSON responses for large organizations
- [ ] Export to CSV/Excel
- [ ] Database storage for historical reports
- [ ] Scheduled automatic reports
- [ ] Web UI dashboard
- [ ] Team-level access reporting
- [ ] Permission change audit log

## Troubleshooting

### "Invalid GitHub Token"
- Verify token is valid and has required scopes
- Check `GITHUB_TOKEN` environment variable is set
- Ensure token has `repo` and `read:org` scopes

### "Rate Limit Exceeded"
- Wait for the rate limit window to reset (typically 1 hour)
- Use a token with higher rate limits (GitHub Apps can have higher limits)
- Implement caching on client side

### "Organization Not Found"
- Verify organization name is correct
- Ensure token has visibility of the organization
- Check organization is not private or restricted

## Contributing

Contributions are welcome! Please follow the existing code style and add tests for new features.

## License

MIT License - see LICENSE file for details

## Support

For issues and questions, please create an issue on GitHub or contact the maintainers.
