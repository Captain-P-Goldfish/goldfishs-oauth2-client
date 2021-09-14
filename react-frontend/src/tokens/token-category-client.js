import ScimClient from "../scim/scim-client";
import {SEARCH_REQUEST_URI, TOKEN_CATEGORY_ENDPOINT, TOKEN_CATEGORY_URI} from "../scim/scim-constants";

export class TokenCategoryClient
{
  
  constructor()
  {
    this.scimClient = new ScimClient(TOKEN_CATEGORY_ENDPOINT, () =>
    {
    });
  }
  
  createCategory(name, onSuccess, onError)
  {
    let category = {
      schemas: [TOKEN_CATEGORY_URI],
      name: name
    };
    this.scimClient.createResource(category)
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
  
  listCategories({
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
  
  updateCategory(id, name, onSuccess, onError)
  {
    let category = {
      schemas: [TOKEN_CATEGORY_URI],
      name: name
    };
    this.scimClient.updateResource(category, id)
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
  
  deleteCategory(category, onSuccess, onError)
  {
    this.scimClient.deleteResource(category.id)
        .then(response =>
              {
                if (response.success)
                {
                  onSuccess(category);
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
