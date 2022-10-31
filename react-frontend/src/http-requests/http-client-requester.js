import React, {useContext, useEffect, useRef, useState} from "react";
import {Alert, Container, Tab, Tabs} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {HTTP_REQUESTS_ENDPOINT, httpHeaderToScimJson, scimHttpHeaderToString} from "../scim/scim-constants";
import {ScimClient2} from "../scim/scim-client-2";
import {Optional} from "../services/utils";
import {FaInfoCircle} from "react-icons/fa";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import {RequestGroupMenuBar} from "./request-group-menu-bar";
import {RequestGroupContext, RequestGroupProvider} from "./request-group-provider";
import {HttpRequestContext, HttpRequestProvider} from "./http-request-provider";
import {HttpRequestMenuBar, HttpResponse} from "./http-request-menu-bar";
import {HttpClientSettings} from "./http-client-settings";
import {toHeaderString} from "./header-utils";
import {useUniqueArray} from "../services/array-utils";
import {LoadingSpinner} from "../base/form-base";
import {toHttpResponseForHistory} from "./response-builder";

export function HttpClientRequester(props)
{
    
    const [activeTab, setActiveTab] = useState(1);
    const [currentHttpClientSettings, setCurrentHttpClientSettings] = useState({
        id: -1,
        requestTimeout: 5,
        connectionTimeout: 5,
        socketTimeout: 5,
        useHostnameVerifier: true,
        proxyReference: undefined,
        tlsClientAuthAliasReference: undefined
    });
    
    function saveHttpClientSettings(httpClientSettings)
    {
        setCurrentHttpClientSettings(httpClientSettings);
    }
    
    function onTabSelect(key)
    {
        setActiveTab(key);
    }
    
    return <React.Fragment>
        
        <h1>HTTP Client</h1>
        
        <HttpRequestProvider>
            <RequestGroupProvider>
                <Container>
                    <Row>
                        <Col sm={"2"}>
                            <RequestGroupMenuBar />
                        </Col>
                        <Col sm={"10"}>
                            <Tabs activeKey={activeTab} onSelect={onTabSelect}>
                                <Tab eventKey={1} title={"HTTP Request"}
                                     onClick={() => setActiveTab(1)}>
                                    <HttpRequestDetails httpClientSettings={currentHttpClientSettings} />
                                </Tab>
                                <Tab eventKey={2} title={"HTTP Client Settings"}
                                     onClick={() => setActiveTab(2)}>
                                    <HttpClientSettings clientSettings={currentHttpClientSettings}
                                                        save={saveHttpClientSettings} />
                                </Tab>
                                <Tab eventKey={3} title={"Saved Requests"}
                                     onClick={() => setActiveTab(3)}>
                                    <HttpRequestMenuBar />
                                </Tab>
                            </Tabs>
                        </Col>
                    </Row>
                </Container>
            </RequestGroupProvider>
        </HttpRequestProvider>
    
    </React.Fragment>;
}

export function HttpRequestDetails({httpClientSettings})
{
    const [httpResponses, setHttpResponses, isResponseInsertable,
              addResponse, updateResponse, removeResponse] = useUniqueArray([],
        response => response.meta.lastModified);
    const [error, setError] = useState();
    
    return <React.Fragment>
        
        <Alert dismissible
               show={new Optional(error).isPresent()}
               variant={"danger"}
               onClose={() => setError(null)}>
            <pre className={"mb-0"}>{error}</pre>
        </Alert>
        
        <HttpRequest httpClientSettings={httpClientSettings}
                     onSuccess={resource =>
                     {
                         setError(null);
                         addResponse(resource);
                     }}
                     onError={errorResponse =>
                     {
                         setError(errorResponse.status + ": " + errorResponse.detail);
                     }} />
        
        <Container>
            <Row>
                <Col>
                    {
                        httpResponses.map((response, index) =>
                        {
                            return <HttpResponse key={index}
                                                 responseStatus={response.responseStatus}
                                                 httpHeader={scimHttpHeaderToString(
                                                     response.responseHeaders)}
                                                 onClose={() =>
                                                 {
                                                     console.log("test");
                                                     removeResponse(response);
                                                 }}
                                                 responseBody={response.responseBody}
                                                 lastModified={response.meta.lastModified} />;
                        })
                    }
                </Col>
            </Row>
        </Container>
    
    </React.Fragment>;
}

export function HttpRequest({
                                httpClientSettings,
                                onSuccess,
                                onError
                            })
{
    
    const requestGroupContext = useContext(RequestGroupContext);
    const httpRequestContext = useContext(HttpRequestContext);
    
    let httpRequest = httpRequestContext.httpRequest.current;
    let group = requestGroupContext.selectedGroup.current;
    
    const httpMethods = ["POST", "GET", "PUT", "PATCH", "DELETE"];
    const selectedMethodRef = useRef();
    const urlRef = useRef();
    const httpHeaderRef = useRef();
    const requestBodyRef = useRef();
    const requestNameRef = useRef();
    
    const [isLoading, setIsLoading] = useState(false);
    
    useEffect(() =>
    {
        requestNameRef.current.value = new Optional(httpRequest).map(request => request.name).orElse("");
        selectedMethodRef.current.value = new Optional(httpRequest).map(request => request.httpMethod).orElse("");
        urlRef.current.value = new Optional(httpRequest).map(request => request.url).orElse("");
        httpHeaderRef.current.value = new Optional(httpRequest).map(request => toHeaderString(request.requestHeaders))
                                                               .orElse("");
        requestBodyRef.current.value = new Optional(httpRequest).map(request => request.requestBody).orElse("");
    }, [httpRequestContext.httpRequest.current]);
    
    function sendHttpRequest()
    {
        setIsLoading(true);
        let httpRequest = {
            groupName: group?.name,
            name: requestNameRef.current.value,
            httpMethod: selectedMethodRef.current.value,
            url: urlRef.current.value,
            requestHeaders: httpHeaderToScimJson(httpHeaderRef.current.value),
            requestBody: requestBodyRef.current.value,
            "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpClientSettings": httpClientSettings
                                                                                   || {}
        };
        let scimClient = new ScimClient2();
        let afterSuccess = newResource =>
        {
            onSuccess(newResource);
            if (httpRequestContext.httpRequest.current.name === newResource.name)
            {
                requestGroupContext.updateHttpRequest(httpRequestContext.httpRequest.current, newResource);
                httpRequestContext.addHttpResponse(toHttpResponseForHistory(newResource));
            }
            else
            {
                requestGroupContext.addHttpRequest(newResource);
            }
            httpRequestContext.httpRequest.current = newResource;
            setIsLoading(false);
        };
        let afterError = errorResponse =>
        {
            onError(errorResponse);
            setIsLoading(false);
        };
        scimClient.createResource(HTTP_REQUESTS_ENDPOINT, httpRequest, afterSuccess, afterError);
    }
    
    return <Container>
        <Row>
            <Col xs={2}>
                <Form.Control as="select" name={"httpMethod"}
                              ref={selectedMethodRef}
                              defaultValue={httpMethods[1]}>
                    {
                        httpMethods.map(method =>
                        {
                            return <option key={method} value={method}>
                                {method}
                            </option>;
                        })
                    }
                </Form.Control>
            </Col>
            <Col>
                <Form.Control name={"url"} ref={urlRef}
                              placeholder={"https://localhost:8443/my-application"}>
                </Form.Control>
            </Col>
        </Row>
        <Row className={"mt-2"}>
            <Col>
                <h4>HTTP Header</h4>
                <Form.Control id={"http-header-area"} as={"textarea"}
                              ref={httpHeaderRef}
                              defaultValue={new Optional(httpHeaderRef.current).map(r => r.value).orElse("")} />
            </Col>
        </Row>
        <Row className={"mt-2"}>
            <Col>
                <h4>Request Body</h4>
                <Form.Control id={"request-body-area"} as={"textarea"}
                              style={{minHeight: "30vh"}}
                              ref={requestBodyRef} />
                
                <Row className={"mt-3"}>
                    <Col sm={"3"}>
                        <Button onClick={() => sendHttpRequest()}>send request <LoadingSpinner
                            show={isLoading} /></Button>
                    </Col>
                    {
                        new Optional(group).isPresent() &&
                        <React.Fragment>
                            <Col sm={"2"}>
                                <Form.Label className={"m-0 mt-2"} htmlFor={"request-name"}>Request Name</Form.Label>
                                <OverlayTrigger placement={'top'}
                                                overlay={
                                                    <Tooltip id={"tooltip-test"}>
                                                        When setting a name the request will be saved within the
                                                        database.
                                                        If the name does already exist the next response will be added
                                                        to
                                                        the history of the saved request. If left empty nothing will be
                                                        stored within the database
                                                    </Tooltip>
                                                }>
                                    <Button variant="link" className={"m-0 p-0 text-warning"}>
                                        <FaInfoCircle className={"ms-2 mb-3"} />
                                    </Button>
                                </OverlayTrigger>
                            
                            </Col>
                            <Col>
                                <Form.Control id={"request-name"}
                                              ref={requestNameRef} />
                            </Col>
                        </React.Fragment>
                    }
                </Row>
            </Col>
        </Row>
    </Container>;
}




