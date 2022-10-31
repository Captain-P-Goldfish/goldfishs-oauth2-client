import React, {useContext, useRef} from 'react';
import {Optional, useScimErrorResponse} from "../services/utils";
import {useUniqueArray} from "../services/array-utils";
import {ScimClient2} from "../scim/scim-client-2";
import {HTTP_REQUESTS_ENDPOINT} from "../scim/scim-constants";
import {HttpRequestContext} from "./http-request-provider";

export const RequestGroupContext = React.createContext();
export const RequestGroupUpdateContext = React.createContext();

export function RequestGroupProvider({children})
{
    
    const requestGroup = useRef({});
    
    const [setErrorResponse] = useScimErrorResponse();
    const [httpRequests, setHttpRequests, isInsertable,
              addHttpRequest, updateHttpRequest, removeHttpRequest] =
              useUniqueArray([], httpRequest => httpRequest.name);
    const httpRequestContext = useContext(HttpRequestContext);
    
    function loadHttpRequests()
    {
        let scimClient = new ScimClient2();
        scimClient.listResources({
            resourcePath: HTTP_REQUESTS_ENDPOINT,
            filter: "groupName eq \"" + requestGroup.current.name + "\"",
            onSuccess: listResponse => {
                setHttpRequests([...listResponse.Resources || []]);
                httpRequestContext.undoSelection();
            },
            onError: errorResponse => setErrorResponse(errorResponse)
        });
    }
    
    function updateRequestGroup(selectedRequestGroup)
    {
        let requestGroupOptional = new Optional(selectedRequestGroup).map(o => Object.keys(o).length === 0 ? null : o);
        requestGroup.current = requestGroupOptional.get();
        if (requestGroupOptional.isPresent())
        {
            loadHttpRequests();
        }
        else
        {
            setHttpRequests([]);
        }
    }
    
    let groupContext = {
        selectedGroup: requestGroup,
        httpRequests: httpRequests,
        httpRequestInsertable: isInsertable,
        addHttpRequest: addHttpRequest,
        updateHttpRequest: updateHttpRequest,
        removeHttpRequest: removeHttpRequest
    };
    
    return (
        <RequestGroupContext.Provider value={groupContext}>
            <RequestGroupUpdateContext.Provider
                value={selectedRequestGroup => updateRequestGroup(selectedRequestGroup)}>
                {children}
            </RequestGroupUpdateContext.Provider>
        </RequestGroupContext.Provider>
    );
}
