import React, {useEffect, useState} from "react";
import {Alert, Container} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {XLg} from "react-bootstrap-icons";
import {HttpClientMenu} from "./http-client-menu";

export function HttpClientRequester(props)
{
  
  const [selectedMenuEntry, setSelectedMenuEntry] = useState("unattached");
  
  return <React.Fragment>
    
    <h1>HTTP Client</h1>
    
    <Container>
      <Row>
        <Col sm={"2"}>
          <HttpClientMenu selectMenuEntry={setSelectedMenuEntry} />
        </Col>
        <Col>
          {/*<Row>*/}
            {/*<HttpRequestSelections  />*/}
          {/*</Row>*/}
          <Row>
            <HttpRequestDetails menuEntry={selectedMenuEntry} />
          </Row>
        </Col>
      </Row>
    </Container>
  
  </React.Fragment>;
}

function HttpRequestSelections()
{
  
  return <React.Fragment>
    <h3>Selections</h3>
  </React.Fragment>;
}

function HttpRequestDetails(props)
{
  
  return <React.Fragment>
    
    <HttpRequest menuEntry={props.menuEntry || ""} />
    
    <Container>
      <Row>
        <Col>
          <HttpResponse responseStatus={200} httpHeader={""} responseBody={"hello world"} />
          <HttpResponse responseStatus={300} httpHeader={""} responseBody={"hello world"} />
          <HttpResponse responseStatus={400} httpHeader={""} responseBody={"hello world"} />
        </Col>
      </Row>
    </Container>
  
  </React.Fragment>;
}

function HttpRequest(props)
{
  
  const httpMethods = ["POST", "GET", "PUT", "PATCH", "DELETE"];
  const [selectedMethod, setSelectedMethod] = useState(httpMethods[0]);
  const [url, setUrl] = useState(props.menuEntry);
  const [httpHeader, setHttpHeader] = useState("");
  const [requestBody, setRequestBody] = useState("");
  
  useEffect(() =>
            {
              setUrl("https://" + props.menuEntry);
            });
  
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
        
        <Button className={"mt-3"}>send request</Button>
      </Col>
    </Row>
  </Container>;
}

function HttpResponse(props)
{
  
  const [showResponse, setShowResponse] = useState(true);
  
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
    <pre id={"response-http-header"}>
      {props.responseBody}
    </pre>
  </Alert>;
}
