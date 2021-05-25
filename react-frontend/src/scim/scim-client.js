import {Optional, toBase64} from "../services/utils";

export default class ScimClient
{

    constructor(resourcePath, setState)
    {
        this.resourcePath = resourcePath;
        this.setState = setState;
        this.resetErrors = this.resetErrors.bind(this);
        this.handleError = this.handleError.bind(this);
        this.createResource = this.createResource.bind(this);
        this.getErrors = this.getErrors.bind(this);
        this.isLoading = this.isLoading.bind(this);
    }

    isLoading(value)
    {
        this.setState({isLoading: value});
    }

    resetErrors()
    {
        this.setState({errors: {}});
    }

    handleError(jsonPromise)
    {
        jsonPromise.then(errorResponse =>
        {
            let errors = {};
            if (errorResponse.errors === undefined)
            {
                errors.errorMessages = new Optional(errors.errorMessages).orElse([]);
                if (errorResponse.detail === undefined)
                {
                    errors.errorMessages.push(JSON.stringify(errorResponse));
                }
                else
                {
                    errors.errorMessages.push(errorResponse.detail);
                }
            }
            else
            {
                errors = errorResponse.errors;
            }
            new Optional(this.setState).ifPresent(method => method({errors: errors}));
        })
    }

    async createResource(resource)
    {
        this.isLoading(true);
        this.resetErrors();
        return await fetch(this.resourcePath, {
            method: "POST",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(resource)
        }).then(response =>
        {
            this.isLoading(false);
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
        this.isLoading(true);
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "GET"
        }).then(response =>
        {
            this.isLoading(false);
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
        this.isLoading(true);
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
            this.isLoading(false);
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
        this.isLoading(true);
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "PUT",
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(resource)
        }).then(response =>
        {
            this.isLoading(false);
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
        this.isLoading(true);
        this.resetErrors();
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "PATCH",
            body: JSON.stringify(patchBody)
        }).then(response =>
        {
            this.isLoading(false);
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
        this.isLoading(true);
        this.resetErrors();
        return fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "DELETE"
        }).then(response =>
        {
            this.isLoading(false);
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

    /**
     * accesses the the form-reference with name "this.formReference" reads its input and select fields and builds a
     * scim resource from it that will be used in the following request
     */
    async getResourceFromFormReference(formReference)
    {
        let scimResource = {};

        let addNestedField = function (jsonObject, objectName)
        {
            if (new Optional(jsonObject).map(val => val[objectName]).isEmpty())
            {
                return jsonObject[objectName] = {};
            }
            return jsonObject[objectName];
        }

        let handleInputField = async function (inputfield)
        {
            let name = inputfield.name;
            let parts = name.split(".")
            let currentObject = scimResource;
            for (let i = 0; i < parts.length - 1; i++)
            {
                currentObject = addNestedField(currentObject, parts[i]);
            }

            if (inputfield.type === 'file' && inputfield.files !== undefined && inputfield.files.length === 1)
            {
                currentObject[parts[parts.length - 1]] = await toBase64(inputfield.files[0]);
            }
            else if (inputfield.type === 'number')
            {
                currentObject[parts[parts.length - 1]] = inputfield.valueAsNumber;
            }
            else
            {
                currentObject[parts[parts.length - 1]] = inputfield.value;
            }
        };

        let formInputFields = Array.from(formReference.current.getElementsByTagName('input'));
        let formSelectFields = Array.from(formReference.current.getElementsByTagName('select'));
        let allFormFields = formInputFields.concat(formSelectFields);

        for (let inputField of allFormFields)
        {
            await handleInputField(inputField);
        }

        return scimResource;
    }

    getErrors(state, fieldName)
    {
        return new Optional(state).map(val => val.errors)
                                  .map(val => val.fieldErrors)
                                  .map(fieldErrors => fieldErrors[fieldName])
                                  .orElse([]);
    }
}


