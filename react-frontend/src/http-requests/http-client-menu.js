import React, {useEffect, useState} from "react";
import {InnerMenubar} from "../services/inner-menubar";
import {ScimClient2} from "../scim/scim-client-2";
import {HTTP_REQUEST_CATEGORIES_ENDPOINT} from "../scim/scim-constants";
import * as lodash from "lodash";
import {ErrorMessageList, ErrorMessagesAlert} from "../base/form-base";

export function HttpClientMenu(props)
{
  
  const [menuEntries, setMenuEntries] = useState([]);
  const [errorResponse, setErrorResponse] = useState({});
  
  useEffect(() =>
            {
              let scimClient = new ScimClient2();
              scimClient.listResources({
                                         resourcePath: HTTP_REQUEST_CATEGORIES_ENDPOINT,
                                         onSuccess: listResponse => setMenuEntries([...listResponse.Resources || []]),
                                         onError: errorResponse => setErrorResponse(errorResponse)
                                       });
            }, []);
  
  function selectMenuEntry(menuEntry)
  {
      props.selectMenuEntry(menuEntry);
  }
  
  function onMenuEntryAdd(menuEntry)
  {
    let scimClient = new ScimClient2();
    let categoryResource = {name: menuEntry};
    scimClient.createResource(HTTP_REQUEST_CATEGORIES_ENDPOINT,
                              categoryResource,
                              responseResource => setMenuEntries([...menuEntries, responseResource]),
                              errorResponse => setErrorResponse(errorResponse));
  }
  
  function onMenuEntryUpdate(oldMenuEntry, newMenuEntry)
  {
    let scimClient = new ScimClient2();
    let menuEntry = lodash.find(menuEntries, ['name', oldMenuEntry]);
    let categoryResource = {
      id: menuEntry.id,
      name: newMenuEntry
    };
    scimClient.updateResource(HTTP_REQUEST_CATEGORIES_ENDPOINT,
                              menuEntry.id,
                              categoryResource,
                              responseResource =>
                              {
                                let copiedList = [...menuEntries];
                                lodash.remove(copiedList, entry => entry.name === oldMenuEntry);
                                copiedList.push(responseResource);
                                setMenuEntries(copiedList);
                              },
                              errorResponse => setErrorResponse(errorResponse));
  }
  
  function onMenuEntryDelete(menuEntryName)
  {
    let scimClient = new ScimClient2();
    let menuEntry = lodash.find(menuEntries, ['name', menuEntryName]);
    scimClient.deleteResource(HTTP_REQUEST_CATEGORIES_ENDPOINT,
                              menuEntry.id,
                              () =>
                              {
                                let copiedList = [...menuEntries];
                                lodash.remove(copiedList, entry => entry.name === menuEntryName);
                                setMenuEntries(copiedList);
                              },
                              errorResponse => setErrorResponse(errorResponse));
  }
  
  return <React.Fragment>
    <ErrorMessagesAlert errors={errorResponse} />
    <ErrorMessageList fieldErrors={errorResponse?.errors?.fieldErrors?.name} />
    <InnerMenubar header={"Request Groups"}
                  entries={lodash.map(menuEntries, 'name').sort((c1, c2) => c1.localeCompare(c2))}
                  onClick={selectMenuEntry}
                  onMenuEntryAdd={onMenuEntryAdd}
                  onMenuEntryUpdate={onMenuEntryUpdate}
                  onMenuEntryDelete={onMenuEntryDelete} />
  </React.Fragment>;
}

