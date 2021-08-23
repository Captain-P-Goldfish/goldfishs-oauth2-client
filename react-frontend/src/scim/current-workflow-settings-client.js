import ScimClient from "./scim-client";
import {CURRENT_WORKFLOW_SETTINGS_ENDPOINT} from "./scim-constants";
import {Optional} from "../services/utils";

export default class CurrentWorkflowSettingsClient
{

    constructor(setState)
    {
        this.scimClient = new ScimClient(CURRENT_WORKFLOW_SETTINGS_ENDPOINT, setState);
    }

    updateAuthCodeSettings(openidClientId, redirectUri, queryParameters, callback)
    {
        let resource = {
            authCodeParameters: {
                redirectUri: redirectUri,
                queryParameters: queryParameters
            }
        };
        this.patchResource(openidClientId, resource, callback);
    }

    updateClientCredentialsSettings(openidClientId, scope, callback)
    {
        let resource = {
            clientCredentialsParameters: {
                scope: scope
            }
        };
        this.patchResource(openidClientId, resource, callback);
    }

    updateResourceOwnerPasswordCredentialsSettings(openidClientId, username, password, scope, callback)
    {
        let resource = {
            resourceOwnerPasswordParameters: {
                username: username,
                password: password,
                scope: scope
            }
        };
        this.patchResource(openidClientId, resource, callback);
    }

    patchResource(openIdClientId, resource, callback)
    {
        let patchOperation = {
            schemas: ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
            Operations: [
                {
                    op: "replace",
                    value: resource
                }
            ]
        };

        this.scimClient.patchResource(patchOperation, openIdClientId).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    new Optional(callback).ifPresent(method => method(resource, response.status));
                })
            }
        })
    }
}