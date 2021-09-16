import ScimClient from "../scim/scim-client";
import {
  BULK_ENDPOINT,
  BULK_REQUEST_URI,
  SEARCH_REQUEST_URI,
  TOKEN_STORE_ENDPOINT,
  TOKEN_STORE_URI
} from "../scim/scim-constants";

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
  
  bulkDeleteTokenStores(tokenStoreArray, maxOperations, onSuccess, onError)
  {
    
    function sendBulkRequest(bulkOperations)
    {
      let bulkRequest = {
        "schemas": [BULK_REQUEST_URI],
        "Operations": bulkOperations
      };
      
      fetch(BULK_ENDPOINT, {
        method: "POST",
        headers: {'Content-Type': 'application/scim+json'},
        body: JSON.stringify(bulkRequest)
      })
        .then(response =>
              {
                if (response.status === 200)
                {
                  response.json()
                          .then(resource =>
                                {
                                  let ops = resource.Operations;
                                  let deleteSuccessIds = [];
                                  let deleteFailedIds = [];
                                  ops.forEach(deletedResponseOperations =>
                                              {
                                                if (deletedResponseOperations.status === 204)
                                                {
                                                  deleteSuccessIds.push(deletedResponseOperations.bulkId);
                                                }
                                                else
                                                {
                                                  deleteFailedIds.push(deletedResponseOperations.bulkId);
                                                }
                                              });
                                  onSuccess(deleteSuccessIds, deleteFailedIds);
                                });
                }
                else
                {
                  response.json()
                          .then(errorResponse =>
                                {
                                  onError(errorResponse);
                                });
                }
              });
    }
    
    let operations = [];
    for (let i = 0; i < tokenStoreArray.length; i++)
    {
      let tokenStore = tokenStoreArray[i];
      operations.push(
        {
          method: "DELETE",
          bulkId: tokenStore.id,
          path: "/TokenStore/" + tokenStore.id
        }
      );
      
      if (operations.length === maxOperations)
      {
        sendBulkRequest([...operations]);
        operations = [];
      }
    }
    
    if (operations.length > 0)
    {
      sendBulkRequest(operations);
    }
  }
}
