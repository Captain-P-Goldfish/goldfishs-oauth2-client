import React, {useRef} from 'react';
import {useUniqueArray} from "../services/array-utils";
import {ScimClient2} from "../scim/scim-client-2";
import {HTTP_RESPONSE_HISTORY_ENDPOINT} from "../scim/scim-constants";
import {useScimErrorResponse} from "../services/utils";

export const HttpRequestContext = React.createContext();
export const HttpRequestUpdateContext = React.createContext();

const DEFAULT_HTTP_REQUEST = {
    httpMethod: "GET",
    url: "http://localhost:8080"
};

export function HttpRequestProvider({children})
{
    
    const httpRequest = useRef(DEFAULT_HTTP_REQUEST);
    const [setErrorResponse] = useScimErrorResponse();
    const [httpResponses, setHttpResponses, isInsertable,
              addHttpResponse, updateHttpResponse, removeHttpResponse] = useUniqueArray([],
        response => response.created,
        (val1, val2) =>
        {
            return new Date(val1) - new Date(val2);
        }
    );
    
    function loadResponseHistory()
    {
        let scimClient = new ScimClient2();
        scimClient.getResource(
            HTTP_RESPONSE_HISTORY_ENDPOINT,
            httpRequest.current.id,
            null,
            httpResponseHistory =>
            {
                httpResponseHistory.responseHistory.sort((v1, v2) => new Date(v2.created) - new Date(v1.created));
                setHttpResponses([...httpResponseHistory.responseHistory || []]);
            },
            errorResponse => setErrorResponse(errorResponse)
        );
    }
    
    let httpRequestContext = {
        httpRequest: httpRequest,
        undoSelection: () =>
        {
            httpRequest.current = DEFAULT_HTTP_REQUEST;
            setHttpResponses([]);
        },
        httpResponses: httpResponses,
        addHttpResponse: addHttpResponse,
        updateHttpResponse: updateHttpResponse,
        removeHttpResponse: removeHttpResponse
    };
    
    return (
        <HttpRequestContext.Provider value={httpRequestContext}>
            <HttpRequestUpdateContext.Provider value={selectedHttpRequest =>
            {
                httpRequest.current = selectedHttpRequest;
                loadResponseHistory();
            }}>
                {children}
            </HttpRequestUpdateContext.Provider>
        </HttpRequestContext.Provider>
    );
}
