import React, {useContext, useEffect, useRef, useState} from 'react';
import {Optional, useScimErrorResponse} from "../services/utils";
import {Accordion, Alert, Container, Form, ListGroup, OverlayTrigger, Popover} from "react-bootstrap";
import {RequestGroupContext} from "./request-group-provider";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {HttpRequestContext, HttpRequestUpdateContext} from "./http-request-provider";
import {GoFlame} from "react-icons/go";
import moment from "moment/moment";
import {PencilSquare, Save, Trash, XLg} from "react-bootstrap-icons";
import {ScimClient2} from "../scim/scim-client-2";
import {HTTP_REQUESTS_ENDPOINT, HTTP_RESPONSE_HISTORY_ENDPOINT} from "../scim/scim-constants";
import Button from "react-bootstrap/Button";
import * as lodash from "lodash";
import {isJson} from "../base/utils";
import {JwsOffCanvas} from "../base/form-base";

export function HttpRequestMenuBar()
{
  const [setErrorResponse] = useScimErrorResponse();
  const requestGroupContext = useContext(RequestGroupContext);
  const updateHttpRequestContext = useContext(HttpRequestUpdateContext);
  const httpRequestContext = useContext(HttpRequestContext);
  const [editMode, setEditMode] = useState(false);

  const requestRefName = useRef();

  if (new Optional(requestGroupContext.selectedGroup.current).isEmpty())
  {
    return;
  }

  function updateResourceOnServer(request)
  {
    let scimClient = new ScimClient2();
    let onSuccess = updatedRequest =>
    {
      setEditMode(false);
      requestGroupContext.updateHttpRequest(request, updatedRequest);
    };
    let onError = errorResponse => setErrorResponse(errorResponse);
    let newResource = lodash.cloneDeep(request);
    newResource.name = requestRefName.current.value;
    scimClient.updateResource(HTTP_REQUESTS_ENDPOINT,
      request.id,
      newResource,
      onSuccess,
      onError);
  }

  let isRequestBodyPresent = new Optional(httpRequestContext.httpRequest.current).map(o => o.requestBody)
    .map(s => s.trim() === ''
      ? null : s)
    .isPresent();
  return <Container>
    <Row>
      <Col sm={"3"}>
        <ListGroup defaultActiveKey={"#"}>
          <ListGroup.Item variant={"warning"}>
            Http Requests
          </ListGroup.Item>
          {
            requestGroupContext.httpRequests.length > 0 &&
            requestGroupContext.httpRequests.map(request =>
            {
              return <ListGroup.Item key={request.name}
                                     action
                                     draggable={false}
                                     href={"#" + request.name}
                                     onClick={e =>
                                     {
                                       updateHttpRequestContext(request);
                                     }}>
                {
                  !editMode &&
                  request.name
                }
                {
                  editMode &&
                  <Form.Control type={"text"} ref={requestRefName} defaultValue={request.name}
                                className={"w-75 d-inline"}
                                onClick={e =>
                                {
                                  e.stopPropagation();
                                }}
                                onKeyUp={e =>
                                {
                                  if (e.key === 'Enter')
                                  {
                                    updateResourceOnServer(request);
                                  }
                                }}/>
                }
                {
                  !editMode &&
                  <OverlayTrigger trigger={"click"} placement={"left"} rootClose={true}
                                  overlay={<Popover className={"p-2 align-content-center"}>
                                    <p>Delete entry?</p>
                                    <Button variant={"danger"} className={"me-2"} onClick={e =>
                                    {
                                      let scimClient = new ScimClient2();
                                      let onSuccess = () =>
                                      {
                                        requestGroupContext.removeHttpRequest(request);
                                      };
                                      let onError = errorResponse =>
                                      {
                                        setErrorResponse(errorResponse);
                                      };
                                      scimClient.deleteResource(HTTP_REQUESTS_ENDPOINT,
                                        request.id,
                                        onSuccess,
                                        onError);
                                    }}>Yes</Button>
                                    <Button variant={"secondary"} onClick={e =>
                                    {
                                      e.stopPropagation();
                                      document.body.click(); // will close the popover
                                    }}>No</Button>
                                  </Popover>}>
                    <Trash
                      className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
                      onClick={e =>
                      {
                        e.preventDefault();
                        e.stopPropagation();
                      }}/>
                  </OverlayTrigger>
                }
                {
                  !editMode &&
                  <PencilSquare
                    className={"add-list-item-icon save-icon edit http-category-icon ms-1 mt-2"}
                    onClick={e =>
                    {
                      e.stopPropagation();
                      setEditMode(true);
                    }}/>
                }
                {
                  editMode &&
                  <XLg className={"add-list-item-icon save-icon edit http-category ms-1"}
                       onClick={e =>
                       {
                         e.stopPropagation();
                         setEditMode(false);
                       }}/>
                }
                {
                  editMode &&
                  <Save className={"add-list-item-icon save-icon edit http-category ms-1"}
                        onClick={e =>
                        {
                          e.stopPropagation();
                          updateResourceOnServer(request);
                        }}/>
                }
              </ListGroup.Item>;
            })
          }
          {
            requestGroupContext.httpRequests.length === 0 &&
            <small className={"bg-danger error text-white"}>
              <GoFlame/> No requests present yet
            </small>
          }
        </ListGroup>
      </Col>
      {
        new Optional(httpRequestContext.httpRequest.current).isPresent() &&
        <Col sm={"9"}>
          <ListGroup>
            <ListGroup.Item draggable={false}>
              <Row>
                <Col>
                                    <pre className={"me-3"}>
                                        {httpRequestContext.httpRequest.current.httpMethod} {httpRequestContext.httpRequest.current.url}
                                      <br/>
                                      {
                                        (httpRequestContext.httpRequest.current.requestHeaders || []).map(keyValue =>
                                        {
                                          return <React.Fragment key={keyValue.name}>
                                            {keyValue.name + ": " + keyValue.value}
                                            <br/>
                                          </React.Fragment>;
                                        })
                                      }
                                    </pre>
                  {
                    isRequestBodyPresent &&
                    <Accordion>
                      <Accordion.Item eventKey={httpRequestContext.httpRequest.current.id}>
                        <Accordion.Header>
                          Request Body
                        </Accordion.Header>
                        <Accordion.Body>
                                                <pre>
                                                    {httpRequestContext.httpRequest.current.requestBody}
                                                </pre>
                        </Accordion.Body>
                      </Accordion.Item>
                    </Accordion>
                  }
                  {
                    !isRequestBodyPresent &&
                    "- no request body -"
                  }
                </Col>
              </Row>
            </ListGroup.Item>
          </ListGroup>

          <h5>Response History</h5>
          <ResponseHistory/>
        </Col>
      }
    </Row>
  </Container>;
}

function ResponseHistory()
{

  const historyContext = useContext(HttpRequestContext);

  return (
    <Container>
      <Accordion>
        {
          historyContext.httpResponses.map(response =>
          {
            return <Row id={response.id} key={response.id}>
              <Col>
                <ResponseItem httpResponse={response}/>
              </Col>
            </Row>;
          })
        }
      </Accordion>
    </Container>
  );
}

function ResponseItem({httpResponse})
{
  const [setErrorResponse] = useScimErrorResponse();
  const [showDelete, setShowDelete] = useState(false);
  const httpRequestContext = useContext(HttpRequestContext);

  return <Accordion.Item eventKey={httpResponse.id} className={"response-history-item"}>
    <Accordion.Header className={showDelete ? "alert-danger" : ""}>
      <div className={"w-100"}>
                    <span
                      className={"request-" + responseStatusToVariant(httpResponse.status)}>
                        Status: {httpResponse.status}
                    </span>
        - From:&nbsp;
        <span className={"timestamp"}>
                        {moment(httpResponse.created).format("MMMM Do, yyyy H:mm:ss a Z")}
                    </span>

        <OverlayTrigger trigger={"click"} placement={"left"} rootClose={true}
                        overlay={<Popover className={"p-2 align-content-center"}>
                          <p>Delete entry?</p>
                          <Button variant={"danger"} className={"me-2"} onClick={e =>
                          {
                            e.preventDefault();
                            e.stopPropagation();
                            let scimClient = new ScimClient2();
                            let onSuccess = () => httpRequestContext.removeHttpResponse(httpResponse);
                            let onError = errorResponse => setErrorResponse(errorResponse);
                            scimClient.deleteResource(HTTP_RESPONSE_HISTORY_ENDPOINT,
                              httpResponse.id,
                              onSuccess,
                              onError);
                          }}>Yes</Button>
                          <Button variant={"secondary"} onClick={e =>
                          {
                            e.preventDefault();
                            e.stopPropagation();
                            document.body.click(); // will close the popover
                          }}>No</Button>
                        </Popover>}
        >
          <Trash className={"float-end me-4"} onClick={e =>
          {
            e.preventDefault();
            e.stopPropagation();
          }}/>
        </OverlayTrigger>
      </div>
    </Accordion.Header>
    <Accordion.Body>
      <HttpResponse key={httpResponse.id}
                    responseStatus={httpResponse.status}
                    originalRequest={httpResponse.originalRequest}
                    httpHeader={httpResponse.responseHeaders}
                    responseBody={httpResponse.responseBody}
                    dismissable={false}/>
    </Accordion.Body>
  </Accordion.Item>;
}

export function HttpResponse({
                               responseStatus,
                               originalRequest,
                               httpHeader,
                               responseBody,
                               lastModified,
                               dismissable,
                               onClose
                             })
{

  const [body, setBody] = useState(responseBody || "");
  const [error, setError] = useState();
  const [jsonBody, setJsonBody] = useState();

  useEffect(() =>
  {
    setBody(responseBody);
    if (isJson(body) === true)
    {
      let json = JSON.parse(body);
      setJsonBody(json);
      setBody(JSON.stringify(json, undefined, 2));
    }
  }, [responseBody]);

  return <Alert className={"mt-4"} variant={responseStatusToVariant(responseStatus)}>
    {
      dismissable !== false &&
      <div className="float-end justify-content-end">
        <XLg title="close" id="close-http-response" className={"close-icon"}
             onClick={() => onClose()}/>
      </div>
    }
    {
      new Optional(originalRequest).isPresent() &&
      <React.Fragment>
        <Alert.Heading>Original Request</Alert.Heading>
        <pre className={"response-http-header"}>
          {originalRequest.trim()}
        </pre>
        <hr/>
      </React.Fragment>
    }
    <Alert.Heading>
      HTTP Response - <span className={"h6"}>({moment(lastModified).format("MMMM Do, yyyy H:mm:ss a Z")})</span>
    </Alert.Heading>
    <p>Status: {responseStatus}</p>
    <b>HTTP Header</b>
    <pre className={"response-http-header"}>
      {httpHeader}
    </pre>
    <b>Response Body</b>
    <a className={"ms-3 cursor-pointer"}
       onClick={e =>
       {
         setError(null);
         e.preventDefault();
         try
         {
           setBody(JSON.stringify(jsonBody, undefined, 2));
         } catch (e)
         {
           setError(e.message);
         }

       }}>pretty print JSON
    </a>
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
           } else
           {
             response.text().then(
               responseBody => setError("Could not parse xml: " + responseBody));
           }
         });

       }}>pretty print XML
    </a>
    <pre className={"response-http-header"}>
      {
        jsonBody !== undefined &&
        <>
          {"{\n"}
          {
            Object.keys(jsonBody).map(key =>
            {
              return <>
                {"  "}"{key}": "<JwsOffCanvas key={key} name={key} value={jsonBody[key]} />"{"\n"}
              </>
            })
          }
          {"\n}"}
        </>
      }
    </pre>
    {
      new Optional(error).isPresent() &&
      <Alert variant={"danger"} dismissible onClick={() => setError(null)}>
        {error}
      </Alert>
    }
  </Alert>;
}

function responseStatusToVariant(status)
{
  if (status >= 200 && status < 300)
  {
    return "success";
  } else if (status >= 400 && status < 600)
  {
    return "danger";
  }
  return "warning";
}
