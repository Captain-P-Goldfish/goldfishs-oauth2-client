import React, {useEffect, useState} from "react";
import {Alert, Badge, Container, ListGroup} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {XLg} from "react-bootstrap-icons";
import {HTTP_REQUESTS_ENDPOINT, httpHeaderToScimJson, scimHttpHeaderToString} from "../scim/scim-constants";
import {ScimClient2} from "../scim/scim-client-2";
import {Optional} from "../services/utils";

export function HttpClientRequester(props)
{

    const [selectedGroup, setSelectedGroup] = useState("unattached");

    return <React.Fragment>

        <h1>HTTP Client</h1>

        <Container>
            <Row>
                <Col sm={"2"}>
                </Col>
                <Col sm={"10"}>
                    <HttpRequestDetails group={selectedGroup} />
                </Col>
            </Row>
        </Container>

    </React.Fragment>;
}

function HttpRequestSelections(props)
{

    const [categories, setCategories] = useState(props.categories);

    return <React.Fragment>
        <ListGroup as="ol">
            {
                categories.map(category =>
                {
                    return <CategorySelection key={category.name} category={category} />;
                })
            }
        </ListGroup>
    </React.Fragment>;
}

function CategorySelection(props)
{
    return <ListGroup.Item as="li" className="d-flex justify-content-between align-items-start">
        <div className="me-auto">
            <div className="fw-bold">{props.category.name}</div>
            <ListGroup className={"ms-3 w-100"}>
                {
                    props.category.requests.map(request =>
                    {
                        return <ListGroup.Item key={request.name}
                                               action href={"#" + request.name}
                                               variant={"secondary"}>
                            {request.name}
                        </ListGroup.Item>;
                    })
                }
            </ListGroup>
        </div>

        <Badge className={"bg-primary rounded-pill"}>
            {props.category.requests.length}
        </Badge>
    </ListGroup.Item>;
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

        <HttpRequest menuEntry={props.group || ""}
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
                                                 httpHeader={scimHttpHeaderToString(response.responseHeaders)}
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

    useEffect(() =>
    {
        setUrl(new Optional(props.url).orElse("http://localhost:8080"));
    }, [props.url]);

    function sendHttpRequest()
    {
        let httpRequest = {
            httpMethod: selectedMethod,
            url: url,
            requestHeaders: httpHeaderToScimJson(httpHeader),
            requestBody: requestBody,
            "urn:ietf:params:scim:schemas:captaingoldfish:2.0:HttpClientSettings": props.httpClientSettings || {}
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
                              value={requestBody}
                              onChange={e => setRequestBody(e.target.value)} />

                <Button className={"mt-3"} onClick={() => sendHttpRequest()}>send request</Button>
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
               setError(null)
               e.preventDefault();
               try
               {
                   let json = JSON.parse(body);
                   setBody(JSON.stringify(json, undefined, 2));
               } catch (e)
               {
                   setError(e.message)
               }

           }}>pretty print JSON</a>
        <a className={"ms-3 cursor-pointer"}
           onClick={e =>
           {
               setError(null)
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
                       response.text().then(responseBody => setError("Could not parse xml: " + responseBody));
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
