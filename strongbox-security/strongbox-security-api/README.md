
This is the Strongbox security API.


# Notes on Access

Anonymous use should only allow read access.
Anonymous access should be allowed by default.

Secured methods should check for isAuthenticationRequired() per repository.
 - If authentication is required, check the subject's principals for the required ones.
 - If anonymous use is allowed, skip any checks.

Each repository should have it's own list of users allowed to authenticate .
