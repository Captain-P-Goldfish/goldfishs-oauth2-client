import {Optional} from "../services/utils";

export class ScimClient2
{
  
  createResource(resourcePath, resource, onSuccess, onError)
  {
    fetch(resourcePath, {
      method: "POST",
      headers: {'Content-Type': 'application/scim+json'},
      body: JSON.stringify(resource)
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 201,
                status: response.status,
                resource: response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
  getResource(resourcePath, id, params, onSuccess, onError)
  {
    let searchParams = new Optional(params).map(parameters => "?" + new URLSearchParams(parameters).toString())
                                           .orElse("");
    let url = resourcePath + new Optional(id).map(val => "/" + encodeURIComponent(val)).orElse("") + searchParams;
    
    fetch(url, {
      method: "GET"
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 201,
                status: response.status,
                resource: response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
  listResources({
                  resourcePath,
                  startIndex,
                  count,
                  filter,
                  sortBy,
                  sortOrder,
                  attributes,
                  excludedAttributes,
                  onSuccess,
                  onError
                } = {})
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
    
    let requestUrl = resourcePath + query;
    
    fetch(requestUrl, {
      method: "GET"
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 200,
                status: response.status,
                resource: response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
  updateResource(resourcePath, id, resource, onSuccess, onError)
  {
    fetch(resourcePath + "/" + encodeURIComponent(id), {
      method: "PUT",
      headers: {'Content-Type': 'application/scim+json'},
      body: JSON.stringify(resource)
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 200,
                status: response.status,
                resource: response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
  patchResource(resourcePath, id, patchBody, onSuccess, onError)
  {
    fetch(resourcePath + "/" + encodeURIComponent(id), {
      method: "PATCH",
      headers: {'Content-Type': 'application/scim+json'},
      body: JSON.stringify(patchBody)
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 200,
                status: response.status,
                resource: response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
  deleteResource(resourcePath, id, onSuccess, onError)
  {
    fetch(resourcePath + "/" + encodeURIComponent(id), {
      method: "DELETE"
    }).then(response =>
            {
              let tmpResponse = {
                success: response.status === 204,
                status: response.status,
                resource: response.status === 204 ? undefined : response.json()
              };
              if (tmpResponse.success)
              {
                new Optional(onSuccess).ifPresent(func => func());
              }
              else
              {
                new Optional(onError).ifPresent(func => tmpResponse.resource.then(json => func(json)));
              }
              return tmpResponse;
            });
  }
  
}
