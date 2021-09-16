import ScimClient from "../scim/scim-client";
import {SEARCH_REQUEST_URI, TOKEN_STORE_ENDPOINT, TOKEN_STORE_URI} from "../scim/scim-constants";

export class TokenStoreClient
{
  constructor()
  {
    this.scimClient = new ScimClient(TOKEN_STORE_ENDPOINT, () =>
    {
    });
  }
  
  createTokenStore(name, token, categoryId, onSuccess, onError)
  {
    let tokenStore = {
      schemas: [TOKEN_STORE_URI],
      name: name,
      token: token,
      categoryId: categoryId
    };
    this.scimClient.createResource(tokenStore)
        .then(response =>
              {
                if (response.success)
                {
                  response.resource.then(resource =>
                                         {
                                           onSuccess(resource);
                                         });
                }
                else
                {
                  response.resource.then(errorResponse =>
                                         {
                                           onError(errorResponse);
                                         });
                }
              });
  }
  
  listTokenStores({
                   startIndex,
                   filter,
                   sortBy,
                   sortOrder
                 } = {}, onSuccess, onError)
  {
    let searchRequest = {
      schemas: [SEARCH_REQUEST_URI],
      sortBy: sortBy,
      sortOrder: sortOrder || "ascending",
      filter: filter,
      startIndex: startIndex
    };
    this.scimClient.listResourcesWithPost(searchRequest, onSuccess, onError);
  }
  
  updateTokenStore(id, name, token, categoryId, onSuccess, onError)
  {
    let tokenStore = {
      schemas: [TOKEN_STORE_URI],
      name: name,
      token: token,
      categoryId: categoryId
    };
    this.scimClient.updateResource(tokenStore, id)
        .then(response =>
              {
                if (response.success)
                {
                  response.resource.then(resource =>
                                         {
                                           onSuccess(resource);
                                         });
                }
                else
                {
                  response.resource.then(errorResponse =>
                                         {
                                           onError(errorResponse);
                                         });
                }
              });
  }
  
  deleteTokenStore(token, onSuccess, onError)
  {
    this.scimClient.deleteResource(token.id)
        .then(response =>
              {
                if (response.success)
                {
                  onSuccess(token);
                }
                else
                {
                  response.resource.then(errorResponse =>
                                         {
                                           onError(errorResponse);
                                         });
                }
              });
  }
  
}
