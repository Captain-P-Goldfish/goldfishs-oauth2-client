import {Optional, toBase64} from "../services/utils";

export default class ScimClient
{

    constructor(resourcePath)
    {
        this.resourcePath = resourcePath;
        this.inputFields = {}
        this.onChange = this.onChange.bind(this);
        this.setErrorListener = this.setErrorListener.bind(this);
        this.resetErrors = this.resetErrors.bind(this);
        this.handleError = this.handleError.bind(this);
        this.getResourceObject = this.getResourceObject.bind(this);
        this.createResource = this.createResource.bind(this);
    }

    onChange(name, value)
    {
        if (value === undefined)
        {
            delete this.inputFields[name];
        }
        else
        {
            this.inputFields[name] = value;
        }
    }

    setErrorListener(onError)
    {
        this.onError = onError;
    }

    /**
     * the method {@link #handleChange} adds the current values into the state context under the key "inputFields".
     * This method will read this data and add it to the coming request to the backend
     */
    async getResourceObject()
    {
        let addField = function (jsonObject, objectName)
        {
            if (jsonObject[objectName] === undefined || jsonObject[objectName] === null)
            {
                return jsonObject[objectName] = {};
            }
            return jsonObject[objectName];
        }

        let scimResource = {};

        for (let [key, value] of Object.entries(this.inputFields))
        {
            let parts = key.split(".")
            let currentObject = scimResource;
            for (let i = 0; i < parts.length - 1; i++)
            {
                currentObject = addField(currentObject, parts[i]);
            }
            if (typeof value.name == 'string')
            {
                currentObject[parts[parts.length - 1]] = await toBase64(value);
            }
            else
            {
                currentObject[parts[parts.length - 1]] = value;
            }
        }

        return scimResource;
    }


    handleError(jsonPromise)
    {
        jsonPromise.then(errorResponse =>
        {
            this.resetErrors();
            if (errorResponse.errors === undefined)
            {
                if (errorResponse.detail === undefined)
                {
                    this.errors.errorMessages.push(JSON.stringify(errorResponse));
                }
                else
                {
                    this.errors.errorMessages.push(errorResponse.detail);
                }
            }
            else
            {
                this.errors = errorResponse.errors;
            }
            new Optional(this.onError).ifPresent(method => method(this.errors));
        })
    }

    resetErrors()
    {
        this.errors = {errorMessages: [], fieldErrors: {}};
    }

    async createResource(resource)
    {
        this.resetErrors();

        return await fetch(this.resourcePath, {
            method: "POST",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(resource)
        }).then(response =>
        {
            if (response.status === 201)
            {
                return {
                    success: true,
                    status: response.status,
                    resource: response.json()
                };
            }
            else
            {
                let tmpResponse = {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
                this.handleError(tmpResponse.resource);
                return tmpResponse;
            }
        })
    }

    async getResource(id)
    {
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "GET"
        }).then(response =>
        {
            if (response.status === 200)
            {
                return {
                    success: true,
                    status: response.status,
                    resource: response.json()
                };
            }
            else
            {
                let tmpResponse = {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
                this.handleError(tmpResponse.resource);
                return tmpResponse;
            }
        })
    }

    listResources({startIndex, count, filter, sortBy, sortOrder, attributes, excludedAttributes} = {})
    {
        this.resetErrors();
        let startIndexParam = new Optional(startIndex).map(val => "startIndex=" + val).orElse(null);
        let countParam = new Optional(count).map(val => "count=" + val).orElse(null);
        let filterParam = new Optional(filter).map(val => "filter=" + encodeURI(val)).orElse(null);
        let sortByParam = new Optional(sortBy).map(val => "sortBy=" + encodeURI(val)).orElse(null);
        let sortOrderParam = new Optional(sortOrder).map(val => "sortOrder=" + val).orElse(null);
        let attributesParam = new Optional(attributes).map(val => "attributes=" + encodeURI(val)).orElse(null);
        let excludedAttributesParam = new Optional(excludedAttributes).map(
            val => "excludedAttributes=" + encodeURI(val)).orElse(null);

        let query = Array.of(startIndexParam, countParam, filterParam, sortByParam, sortOrderParam, attributesParam,
            excludedAttributesParam)
                         .filter(val => val != null)
                         .join("&");

        query = new Optional(query).filter(val => val.length > 0).map(val => "?" + val).orElse("");

        let requestUrl = this.resourcePath + query;

        return fetch(requestUrl, {
            method: "GET"
        }).then(response =>
        {
            if (response.status === 200)
            {
                return {
                    success: true,
                    status: response.status,
                    resource: response.json()
                }
            }
            else
            {
                let tmpResponse = {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
                this.handleError(tmpResponse.resource);
                return tmpResponse;
            }
        })
    }

    async updateResource(resource, id)
    {
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "PUT",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(resource)
        }).then(response =>
        {
            if (response.status === 200)
            {
                return {
                    success: true,
                    status: response.status,
                    resource: response.json()
                };
            }
            else
            {
                let tmpResponse = {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
                this.handleError(tmpResponse.resource);
                return tmpResponse;
            }
        })
    }

    async patchResource(patchBody, id)
    {
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "PATCH",
            body: JSON.stringify(patchBody)
        }).then(response =>
        {
            if (response.status === 200)
            {
                return {
                    success: true,
                    status: response.status,
                    resource: response.json()
                };
            }
            else
            {
                let tmpResponse = {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
                this.handleError(tmpResponse.resource);
                return tmpResponse;
            }
        })
    }

    deleteResource(id)
    {
        this.resetErrors();
        return fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "DELETE"
        }).then(response =>
        {
            if (response.status === 204)
            {
                return {
                    success: true,
                    status: response.status
                };
            }
            else
            {
                this.handleError(response.json());
                return {
                    success: false,
                    status: response.status
                }
            }
        })
    }
}


