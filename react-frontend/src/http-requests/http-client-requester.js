import React, {useEffect, useState} from "react";
import {Alert, Card, Container, Tab, Table, Tabs} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {XLg} from "react-bootstrap-icons";
import {HttpClientMenu} from "./http-client-menu";
import {HTTP_REQUESTS_ENDPOINT, httpHeaderToScimJson, scimHttpHeaderToString} from "../scim/scim-constants";
import {ScimClient2} from "../scim/scim-client-2";
import {Optional} from "../services/utils";
import {HttpClientSettings} from "./http-client-settings";
import {FaInfoCircle} from "react-icons/fa";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import {InnerMenubar} from "../services/inner-menubar";
import * as lodash from "lodash";

export function HttpClientRequester(props)
{
    
    const [activeTab, setActiveTab] = useState(1);
    const [selectedGroup, setSelectedGroup] = useState();
    const [currentHttpClientSettings, setCurrentHttpClientSettings] = useState({
        id: -1,
        requestTimeout: 5,
        connectionTimeout: 5,
        socketTimeout: 5,
        useHostnameVerifier: true,
        proxyReference: undefined,
        tlsClientAuthAliasReference: undefined
    });
    const [selectedHttpRequest, setSelectedHttpRequest] = useState();
    
    function selectNewGroup(group)
    {
        if (new Optional(group).isEmpty())
        {
            setSelectedGroup(null);
            return;
        }
        setSelectedGroup(group);
        // let scimClient = new ScimClient2();
        // let onSuccess = resource => setSelectedGroup(resource);
        // let onError = errorResponse => setError(errorResponse);
        // scimClient.getResource(HTTP_REQUEST_GROUPS_ENDPOINT, group.id, null, onSuccess, onError);
    }
    
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
        
        <Container>
            <Row>
                <Col sm={"2"}>
                    <HttpClientMenu selectMenuEntry={selectNewGroup} />
                </Col>
                <Col sm={"10"}>
                    <Tabs activeKey={activeTab} defaultActiveKey={"http-request"} onSelect={onTabSelect}>
                        <Tab eventKey={1} title={"HTTP Request"}
                             onClick={e => setActiveTab(1)}>
                            <HttpRequestDetails group={selectedGroup} />
                        </Tab>
                        <Tab eventKey={2} title={"HTTP Client Settings"}
                             onClick={e => setActiveTab(2)}>
                            <HttpClientSettings clientSettings={currentHttpClientSettings}
                                                save={saveHttpClientSettings} />
                        </Tab>
                        {
                          new Optional(selectedGroup).isPresent() &&
                          <Tab eventKey={3} title={"Saved Requests"}
                               onClick={e => setActiveTab(3)}>
                              <HttpRequestSelections selectedGroup={selectedGroup}
                                                     selectedHttpRequest={selectedHttpRequest}
                                                     selectHttpRequestsTab={() => setActiveTab(3)}
                                                     doRequestSelection={httpRequest =>
                                                     {
                                                         setSelectedHttpRequest(httpRequest);
                                                         setActiveTab(1);
                                                     }} />
                          </Tab>
                        }
                    </Tabs>
                </Col>
            </Row>
        </Container>
    
    </React.Fragment>;
}

function HttpRequestSelections(props)
{
    
    const [savedRequests, setSavedRequests] = useState([]);
    const [selectedSavedRequest, setSelectedSavedRequest] = useState();
    const [errorResponse, setErrorResponse] = useState([]);
    
    useEffect(() =>
    {
        setErrorResponse(null);
        let scimClient = new ScimClient2();
        let onSuccess = listedResources =>
        {
            setSavedRequests(listedResources.Resources || []);
            if (listedResources.itemsPerPage > 0)
            {
                props.selectHttpRequestsTab();
            }
        };
        let onError = errorResponse =>
        {
            setErrorResponse(errorResponse);
        };
        scimClient.listResources({
            resourcePath: HTTP_REQUESTS_ENDPOINT,
            filter: "groupName eq \"" + props.selectedGroup.name + "\"",
            onSuccess: onSuccess,
            onError: onError
        });
    }, [props.selectedGroup]);
    
    return <Row>
        {
          savedRequests.length === 0 &&
          <Alert variant={"info"}>
              No requests were saved for group "{props.selectedGroup.name}" yet
          </Alert>
        }
        <Col sm={3}>
            <InnerMenubar headerOff={true}
                          entries={lodash.map(savedRequests, 'name').sort((c1, c2) => c1.localeCompare(c2))}
                          onClick={element =>
                          {
                              setSelectedSavedRequest(lodash.find(savedRequests, {name: element}));
                          }}
                          onMenuEntryUpdate={() =>
                          {
                          }}
                          onMenuEntryDelete={() =>
                          {
                          }}
            />
        </Col>
        {
          new Optional(selectedSavedRequest).isPresent() &&
          <Col sm={9}>
              <Card className={"resource-card w-100 mw-100"}>
                  <Card.Body className={"text-dark"}>
                      <Table>
                          <tbody>
                              <tr className={"border-0"}>
                                  <th className={"w-25"}>
                                      HTTP Method
                                  </th>
                                  <td>
                                      {selectedSavedRequest.httpMethod}
                                  </td>
                              </tr>
                              <tr className={"border-0"}>
                                  <th>
                                      URL
                                  </th>
                                  <td>
                                      {selectedSavedRequest.url}
                                  </td>
                              </tr>
                          </tbody>
                      </Table>
                      <Form.Label className={"fw-bold"}>Request Body</Form.Label>
                      <pre>
                          {new Optional(selectedSavedRequest.requestBody).map(s => s.length === 0 ? null : s)
                                                                         .orElse("- no request body -")}
                      </pre>
                      <Form.Label className={"fw-bold"}>Response History</Form.Label>
                  </Card.Body>
              </Card>
          </Col>
        }
    </Row>;
}

export function HttpRequestDetails(props)
{
    const [httpResponses, setHttpResponses] = useState([]);
    const [error, setError] = useState();
    
    return <React.Fragment>
        
        <Alert dismissible
               show={new Optional(error).isPresent()}
               variant={"danger"}
               onClose={() => setError(null)}>
            <pre className={"mb-0"}>{error}</pre>
        </Alert>
        
        <HttpRequest group={props.group}
                     onSuccess={resource =>
                     {
                         setError(null);
                         let newHttpResponses = [...httpResponses];
                         newHttpResponses.splice(0, 0, resource);
                         setHttpResponses(newHttpResponses);
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
                                                 responseBody={response.responseBody} />;
                        })
                    }
                </Col>
            </Row>
        </Container>
    
    </React.Fragment>;
}

export function HttpRequest(props)
{
    
    const httpMethods = ["POST", "GET", "PUT", "PATCH", "DELETE"];
    const [selectedMethod, setSelectedMethod] = useState(props.method || httpMethods[1]);
    const [url, setUrl] = useState(new Optional(props.url).orElse("http://localhost:8080"));
    const [httpHeader, setHttpHeader] = useState(props.httpHeader || "");
    const [requestBody, setRequestBody] = useState(props.requestBody || "");
    const [requestName, setRequestName] = useState("");
    
    useEffect(() =>
    {
        setUrl(new Optional(props.url).orElse("http://localhost:8080"));
    }, [props.url]);
    
    function sendHttpRequest()
    {
        let httpRequest = {
            groupName: props.group?.name,
            name: requestName,
            httpMethod: selectedMethod,
            url: url,
            requestHeaders: httpHeaderToScimJson(httpHeader),
            requestBody: requestBody,
            "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpClientSettings": props.httpClientSettings
                                                                                   || {}
        };
        let scimClient = new ScimClient2();
        scimClient.createResource(HTTP_REQUESTS_ENDPOINT, httpRequest, props.onSuccess, props.onError);
    }
    
    return <Container>
        <Row>
            <Col xs={2}>
                <Form.Control as="select" name={"httpMethod"}
                              value={selectedMethod}
                              onChange={e => setSelectedMethod(e.target.value)}>
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
                <Form.Control name={"url"} value={url} onChange={e => setUrl(e.target.value)}
                              placeholder={"https://localhost:8443/my-application"}>
                </Form.Control>
            </Col>
        </Row>
        <Row className={"mt-2"}>
            <Col>
                <h4>HTTP Header</h4>
                <Form.Control id={"http-header-area"} as={"textarea"}
                              value={httpHeader}
                              onChange={e => setHttpHeader(e.target.value)} />
            </Col>
        </Row>
        <Row className={"mt-2"}>
            <Col>
                <h4>Request Body</h4>
                <Form.Control id={"request-body-area"} as={"textarea"}
                              style={{minHeight: new Optional(props.minHeight).orElse("30vh")}}
                              value={requestBody}
                              onChange={e => setRequestBody(e.target.value)} />
                
                <Row className={"mt-3"}>
                    <Col sm={"3"}>
                        <Button onClick={() => sendHttpRequest()}>send request</Button>
                    </Col>
                    {
                      new Optional(props.group).isPresent() &&
                      <React.Fragment>
                          <Col sm={"2"}>
                              <Form.Label className={"m-0 mt-2"} htmlFor={"request-name"}>Request Name</Form.Label>
                              <OverlayTrigger placement={'top'}
                                              overlay={
                                                  <Tooltip id={"tooltip-test"}>
                                                      When setting a name the request will be saved within the database.
                                                      If the name does already exist the next response will be added to
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
                                            value={requestName}
                                            onChange={e => setRequestName(e.target.value)} />
                          </Col>
                      </React.Fragment>
                    }
                </Row>
            </Col>
        </Row>
    </Container>;
}

export function HttpResponse(props)
{
    
    const [showResponse, setShowResponse] = useState(true);
    const [body, setBody] = useState(props.responseBody || "");
    const [error, setError] = useState();
    
    useEffect(() =>
    {
        setBody(props.responseBody);
    }, [props.responseBody]);
    
    function responseStatusToVariant()
    {
        if (props.responseStatus >= 200 && props.responseStatus < 300)
        {
            return "success";
        }
        else if (props.responseStatus >= 400 && props.responseStatus < 600)
        {
            return "danger";
        }
        return "warning";
    }
    
    return <Alert className={"mt-4"} show={showResponse} variant={responseStatusToVariant()}>
        <div className="float-end justify-content-end">
            <XLg title="close" id="close-http-response" className={"close-icon"}
                 onClick={() => setShowResponse(false)} />
        </div>
        <Alert.Heading>HTTP Response</Alert.Heading>
        <p>Status: {props.responseStatus}</p>
        <b>HTTP Header</b>
        <pre id={"response-http-header"}>
      {props.httpHeader}
    </pre>
        <b>Response Body</b>
        <a className={"ms-3 cursor-pointer"}
           onClick={e =>
           {
               setError(null);
               e.preventDefault();
               try
               {
                   let json = JSON.parse(body);
                   setBody(JSON.stringify(json, undefined, 2));
               } catch (e)
               {
                   setError(e.message);
               }
            
           }}>pretty print JSON</a>
        <a className={"ms-3 cursor-pointer"}
           onClick={e =>
           {
               setError(null);
               e.preventDefault();
               fetch("/pretty-print-xml", {
                   method: "POST",
                   body: body
               }).then(response =>
               {
                   if (response.status === 200)
                   {
                       response.text().then(responseBody => setBody(responseBody));
                   }
                   else
                   {
                       response.text().then(
                         responseBody => setError("Could not parse xml: " + responseBody));
                   }
               });
            
           }}>pretty print XML</a>
        <pre id={"response-http-header"}>
          {body}
        </pre>
        {
          new Optional(error).isPresent() &&
          <Alert variant={"danger"} dismissible onClick={() => setError(null)}>
              {error}
          </Alert>
        }
    </Alert>;
}
