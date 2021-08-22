import React from "react";
import {Collapseable, ErrorListItem} from "../../base/form-base";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {Alert} from "react-bootstrap";

export default function AccessTokenDetailsView(props)
{
    return <div className={"workflow-details"}>
        <AccessTokenRequestView accessTokenDetails={props.accessTokenDetails} />
        <AccessTokenResponseView accessTokenDetails={props.accessTokenDetails} />
    </div>
}

function AccessTokenRequestView(props)
{
    let requestViewContent = function ()
    {
        return <React.Fragment>
            <Collapseable header={"Request Header"}
                          variant={"workflow-details"}
                          content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                        nameValueList={((props.accessTokenDetails || {})
                                                                            .requestHeaders || [])} />} />
            <Collapseable header={"Request Parameter"}
                          variant={"workflow-details"}
                          content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                        nameValueList={((props.accessTokenDetails || {})
                                                                            .requestParams || [])} />} />
        </React.Fragment>
    }
    return <Collapseable header={"Access Token Request Details"}
                         variant={"workflow-details"}
                         content={requestViewContent} />
}

function AccessTokenResponseView(props)
{

    let responseDetails = (props.accessTokenDetails || {});
    let responseStatusCode = responseDetails.statusCode;
    let responseHeaders = responseDetails.responseHeaders || [];
    let plainResponse = responseDetails.plainResponse || "";
    let contentType = (responseHeaders.filter(header => header.name.toLowerCase() === "content-type")[0]
                       || []).value;

    let header = function ()
    {
        return <span>Access Token Response Details
                <span className={"bold"}> (Status: {responseStatusCode})</span>
            </span>
    }
    let responseViewContent = function ()
    {
        return <React.Fragment>
            <Collapseable header={"Response Header"}
                          variant={"workflow-details"}
                          content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                        nameValueList={responseHeaders} />} />
            <Collapseable header={"Access Token Response"}
                          open={true}
                          variant={"workflow-details"}
                          content={() => <AccessTokenResponse contentType={contentType}
                                                              tokenResponse={plainResponse} />} />
        </React.Fragment>
    }
    return <Collapseable header={header()}
                         open={true}
                         variant={"workflow-details"}
                         content={responseViewContent} />
}

function NameValueList(props)
{
    return <React.Fragment>
        {
            props.nameValueList.map((nameValuePair, index) =>
            {
                return <Row key={props.keyPrefix + index}>
                    <Col sm={2}>{nameValuePair.name}</Col>
                    <Col sm={10}>{nameValuePair.value}</Col>
                </Row>
            })
        }
    </React.Fragment>
}

export class AccessTokenResponse extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {errorMessages: []}
    }

    componentDidMount()
    {
        this.parseJsonContent();
    }

    async parseJsonContent()
    {
        let isContentTypeJson = (this.props.contentType || "").toLowerCase().includes("application/json");
        try
        {
            let stateExtension = {};
            stateExtension.json = JSON.parse(this.props.tokenResponse);
            if (!isContentTypeJson)
            {
                stateExtension.errorMessages = [...this.state.errorMessages,
                                                "Found invalid content-type: " + this.props.contentType
                                                + ". Response is valid JSON but content-type header does not match."];
            }
            this.setState(stateExtension)
        } catch (e)
        {
            console.error(e)
            if (isContentTypeJson)
            {
                this.setState({
                    errorMessages: [...this.state.errorMessages, "Expected content to be JSON but could not parse it",
                                    e.message]
                })
            }
        }
    }

    render()
    {
        return (
            <div id={"access-token-response-container"}>
                {
                    this.state.errorMessages.length > 0 &&
                    <Alert variant={"danger"}>
                        <ul className="error-list">
                            {this.state.errorMessages.map((message, index) =>
                                <ErrorListItem key={"error-message-" + index} message={message} />)}
                        </ul>
                    </Alert>
                }
                {
                    this.state.json &&
                    Object.keys(this.state.json).map((key, index) =>
                    {
                        return <Row key={"access-token-response-json-param-" + index}>
                            <Col sm={2}>{key}</Col>
                            <Col sm={10}> {this.state.json[key]} </Col>
                        </Row>
                    })
                }
                {
                    !this.state.json &&
                    <Row key={"access-token-response-json-param-_"}>
                        <Col sm={2}>access_token</Col>
                        <Col sm={10}> {this.props.tokenResponse} </Col>
                    </Row>
                }
            </div>
        )
    }
}
