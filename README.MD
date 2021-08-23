# OAuth2 Client

This project is a test-program that I started developing in order to learn react. It can be used to build and parse JWTs
of type JWS and JWE. It has support for RSA and EC keys and allows the user to access Oauth2/OpenID Connect Providers to
get access tokens for the following grants (see also screenshot):

1. Authorization Code Grant
2. Client Credentials Grant
3. Resource Owner Password Credentials Grant

It allows you to take a direct look at request and response details which can be used for teaching purposes.

The project supports basic-authentication and JWT authentication but the some features on the different grants are still
missing.

![theme-settings](images/auth-code-grant.png)