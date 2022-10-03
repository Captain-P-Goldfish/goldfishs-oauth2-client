import {Optional} from "../services/utils";

export const CERT_URI = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CertificateInfo";
export const CURRENT_WORKFLOW_URI = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CurrentWorkflowSettings";
export const TOKEN_CATEGORY_URI = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:TokenCategory";
export const TOKEN_STORE_URI = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:TokenStore";
export const SEARCH_REQUEST_URI = "urn:ietf:params:scim:api:messages:2.0:SearchRequest";
export const BULK_REQUEST_URI = "urn:ietf:params:scim:api:messages:2.0:BulkRequest";

const BASE_URL = "/scim/v2";
export const ACCESS_TOKEN_REQUEST_ENDPOINT = BASE_URL + "/AccessTokenRequest";
export const APP_INFO_ENDPOINT = BASE_URL + "/AppInfo";
export const SERVICE_PROVIDER_CONFIG_ENDPOINT = BASE_URL + "/ServiceProviderConfig";
export const AUTH_CODE_GRANT_ENDPOINT = BASE_URL + "/AuthCodeGrantRequest";
export const HTTP_CLIENT_SETTINGS_ENDPOINT = BASE_URL + "/HttpClientSettings";
export const JWT_BUILDER_ENDPOINT = BASE_URL + "/JwtBuilder";
export const KEYSTORE_ENDPOINT = BASE_URL + "/Keystore";
export const OPENID_CLIENT_ENDPOINT = BASE_URL + "/OpenIdClient";
export const OPENID_PROVIDER_ENDPOINT = BASE_URL + "/OpenIdProvider";
export const PROXY_ENDPOINT = BASE_URL + "/Proxy";
export const TRUSTSTORE_ENDPOINT = BASE_URL + "/Truststore";
export const CURRENT_WORKFLOW_SETTINGS_ENDPOINT = BASE_URL + "/CurrentWorkflowSettings";
export const TOKEN_CATEGORY_ENDPOINT = BASE_URL + "/TokenCategory";
export const TOKEN_STORE_ENDPOINT = BASE_URL + "/TokenStore";
export const BULK_ENDPOINT = BASE_URL + "/Bulk";
export const HTTP_REQUEST_CATEGORIES_ENDPOINT = BASE_URL + "/HttpRequestGroups";
export const HTTP_REQUESTS_ENDPOINT = BASE_URL + "/HttpRequests";

/**
 * translates a http header string into its SCIM json representation for sending http requests at the backend
 * the structure will look like this:
 * <pre>
 *     [
 *       {
 *           name: Content-Type,
 *           value: application/json
 *       },
 *       ...
 *     ]
 * </pre>
 * @param httpHeaderString the string from a html textfield
 */
export function httpHeaderToScimJson(httpHeaderString)
{
    let lines = httpHeaderString.split('\n');
    let jsonArray = [];
    for (let i = 0; i < lines.length; i++)
    {
        let line = lines[i];
        let keyValue = line.split(':');
        if (keyValue.length === 2)
        {
            let key = keyValue[0];
            let value = keyValue[1];
            jsonArray.push({
                               name: key,
                               value: value
                           });
        }
    }
    return jsonArray;
}

export function scimHttpHeaderToString(scimHttpHeader)
{
    if (new Optional(scimHttpHeader).isEmpty())
    {
        return "";
    }
    let httpHeaderString = "";
    for (let i = 0; i < scimHttpHeader.length; i++)
    {
        let keyValue = scimHttpHeader[i];
        httpHeaderString += keyValue.name + ": " + keyValue.value + "\n";
    }
    return httpHeaderString;
}
