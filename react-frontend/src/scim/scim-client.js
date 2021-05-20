import {Optional} from "../services/utils";

export default class ScimClient
{

    constructor(resourcePath)
    {
        this.resourcePath = resourcePath;
        this.inputFields = {}
    }

    async createResource(resource)
    {
        return await fetch(this.resourcePath, {
            method: "POST",
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
                return {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
            }
        })
    }

    async getResource(id)
    {
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
                return {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
            }
        })
    }

    listResources({startIndex, count, filter, sortBy, sortOrder, attributes, excludedAttributes} = {})
    {
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
                return {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
            }
        })
    }

    async updateResource(resource, id)
    {
        return await fetch(this.resourcePath + "/" + encodeURIComponent(id), {
            method: "PUT",
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
                return {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
            }
        })
    }

    async patchResource(patchBody, id)
    {
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
                return {
                    success: false,
                    status: response.status,
                    resource: response.json()
                }
            }
        })
    }

    deleteResource(id)
    {
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
                return {
                    success: false,
                    status: response.status
                }
            }
        })
    }
}


