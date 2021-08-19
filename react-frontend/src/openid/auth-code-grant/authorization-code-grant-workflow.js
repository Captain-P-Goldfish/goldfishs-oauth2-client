import React, {useState} from "react";
import Col from "react-bootstrap/Col";
import {Collapseable, ErrorListItem, LoadingSpinner} from "../../base/form-base";
import Row from "react-bootstrap/Row";
import Button from "react-bootstrap/Button";
import {CaretDown, CaretRight, XLg} from "react-bootstrap-icons";
import {Alert, Card, Collapse} from "react-bootstrap";

export default class AuthorizationCodeGrantWorkflow extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {}
        this.setState = this.setState.bind(this);
        this.forceUpdate = this.forceUpdate.bind(this);
        this.loadAuthorizationQueryParameterView = this.loadAuthorizationQueryParameterView.bind(this);
        this.getAuthRequestStatus = this.getAuthRequestStatus.bind(this);
        this.loadAuthorizationCodeResponseDetailsView = this.loadAuthorizationCodeResponseDetailsView.bind(this);
    }

    componentWillUnmount()
    {
        if (this.state.interval)
        {
            clearInterval(this.state.interval);
        }
    }

    componentDidMount()
    {
        let state = this.state;
        let forceUpdate = this.forceUpdate
        let getAuthRequestStatus = this.getAuthRequestStatus;
        this.state.interval = setInterval(function ()
        {
            let authRequestStatus = getAuthRequestStatus();
            if (authRequestStatus.success)
            {
                clearInterval(state.interval);
                delete state.interval;
                state.authorizationResponseUrl = authRequestStatus.authorizationResponseUrl;
                forceUpdate();
            }
        }, 200);
    }

    getAuthRequestStatus()
    {
        let code = Math.random().toString(36).substring(2, 15);
        let state = Math.random().toString(36).substring(2, 15);
        return {
            success: true,
            authorizationResponseUrl: "http://localhost:8080/authcode?code=" + code + "&state=" + state
        };
    }

    loadAuthorizationQueryParameterView()
    {
        let authCodeQueryParams = Object.fromEntries(new URL(this.props.requestDetails.authCodeUrl).searchParams);

        return <div className={"workflow-details"}>
            <Collapseable header={"Authorization Request Details"} variant={"workflow-details"} content={() =>
            {
                return <React.Fragment>
                    <Row>
                        <Col sm={2} className={"url-base-value"}>authCodeUrl</Col>
                        <Col sm={10} className={"url-base-value"}>{this.props.requestDetails.authCodeUrl}</Col>
                    </Row>
                    {
                        Object.keys(authCodeQueryParams).map((key, index) =>
                        {
                            return <Row key={"auth-code-request-row-" + index}>
                                <Col sm={2}>{key}</Col>
                                <Col sm={10}>{authCodeQueryParams[key]}</Col>
                            </Row>
                        })
                    }
                </React.Fragment>
            }} />
        </div>
    }

    loadAuthorizationCodeResponseDetailsView()
    {
        if (!this.state.authorizationResponseUrl)
        {
            return null;
        }
        let authorizationResponseUrl = new URL(this.state.authorizationResponseUrl);
        const queryParamsObject = Object.fromEntries(authorizationResponseUrl.searchParams);

        return <div className={"workflow-details"}>
            {
                <React.Fragment>
                    <Collapseable header={"Authorization Response Details"}
                                  open={true}
                                  variant={"workflow-details"}
                                  content={() =>
                                  {
                                      return <React.Fragment>
                                          <Row>
                                              <Col sm={2} className={"url-base-value"}>authResponseUrl</Col>
                                              <Col sm={10} className={"url-base-value"}>
                                                  {this.state.authorizationResponseUrl}
                                              </Col>
                                          </Row>
                                          {
                                              Object.keys(queryParamsObject).map((key, index) =>
                                              {
                                                  return <Row key={"auth-code-response-row-" + index}>
                                                      <Col sm={2}>{key}</Col>
                                                      <Col sm={10}>{queryParamsObject[key]}</Col>
                                                  </Row>
                                              })
                                          }
                                      </React.Fragment>
                                  }}
                    />
                    <AccessTokenView />
                </React.Fragment>
            }
        </div>
    }

    render()
    {
        return (
            <div className={"authorization-code-workflow"}>
                {
                    <AuthorizationCodeGrantDetails
                        content={() =>
                        {
                            return <React.Fragment>
                                {this.loadAuthorizationQueryParameterView()}
                                {this.loadAuthorizationCodeResponseDetailsView()}
                            </React.Fragment>
                        }}
                        responseDetails={this.props.authResponseParameters}
                        remove={this.props.onRemove} />
                }
            </div>
        );
    }
}

function AuthorizationCodeGrantDetails(props)
{
    const [open, setOpen] = useState(true);

    let variant = "dark";
    return (
        <React.Fragment>
            <Alert className={"collapse-header"}
                   variant={variant}
                   onClick={() =>
                   {
                       setOpen(!open);
                   }}
            >
                {
                    open === true &&
                    <CaretDown />
                }
                {
                    open === false &&
                    <CaretRight />
                }
                Authorization Code Grant/Flow
                {
                    props.remove !== undefined &&
                    <XLg onClick={props.remove} className={"remove-collapse"} />
                }
            </Alert>
            <Collapse in={open}>
                <Card className={"workflow-card"}>
                    <Card.Body>
                        {props.content()}
                    </Card.Body>
                </Card>
            </Collapse>
        </React.Fragment>
    );
}

class AccessTokenView extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {};
        this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
        this.loadAccessTokenView = this.loadAccessTokenView.bind(this);
        this.loadAccessTokenRequestView = this.loadAccessTokenRequestView.bind(this);
        this.loadAccessTokenResponseView = this.loadAccessTokenResponseView.bind(this);
    }

    retrieveAccessTokenDetails(e)
    {
        e.preventDefault();
        let accessTokenDetails = {
            requestDetails: {
                requestHeaders: [
                    {
                        name: "User-Agent",
                        value: "apache-http-client"
                    },
                    {
                        name: "Authorization",
                        value: "Basic Z29sZGZpc2g6MTIzNDU2"
                    },
                ],
                requestParams: [
                    {
                        name: "client_id",
                        value: "goldifsh"
                    },
                    {
                        name: "redirect_uri",
                        value: "http://localhost:8080"
                    },
                ]
            },
            response: {
                statusCode: 200,
                responseHeaders: [
                    {
                        name: "Content-Type",
                        value: "application/json"
                    }
                ],
                plainResponse: "{\n"
                               + "  \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\",\n"
                               + "  \"token_type\": \"Bearer\",\n"
                               + "  \"expires_in\": 3600,\n"
                               + "  \"refresh_token\": \"cba\"\n"
                               + "}"
            }
        }
        this.setState({accessTokenDetails: accessTokenDetails})
    }

    loadAccessTokenRequestView()
    {
        let state = this.state;
        let requestViewContent = function ()
        {
            return <React.Fragment>
                <Collapseable header={"Request Header"}
                              variant={"workflow-details"}
                              content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                            nameValueList={(((state.accessTokenDetails || {})
                                                                .requestDetails || {}).requestHeaders || [])} />} />
                <Collapseable header={"Request Parameter"}
                              variant={"workflow-details"}
                              content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                            nameValueList={(((state.accessTokenDetails || {})
                                                                .requestDetails || {}).requestParams || [])} />} />
            </React.Fragment>
        }
        return <Collapseable header={"Access Token Request Details"}
                             variant={"workflow-details"}
                             content={requestViewContent} />
    }

    loadAccessTokenResponseView()
    {
        let responseDetails = ((this.state.accessTokenDetails || {}).response || {});
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

    loadAccessTokenView()
    {
        return <React.Fragment>
            {this.loadAccessTokenRequestView()}
            {this.loadAccessTokenResponseView()}
        </React.Fragment>
    }

    render()
    {
        return <React.Fragment>
            <Button type="submit" onClick={this.retrieveAccessTokenDetails}
                    style={{marginTop: "15px", marginBottom: "15px"}}>
                <LoadingSpinner show={this.props.isLoading} /> Get Access Token
            </Button>
            {this.state.accessTokenDetails && this.loadAccessTokenView()}
        </React.Fragment>
    }
}

function NameValueList(props)
{
    return <React.Fragment>
        {
            props.nameValueList.map((nameValuePair, index) =>
            {
                return <Row key={props.keyPrefix + index}>
                    <Col sm={2}>{nameValuePair.name}</Col>
                    <Col sm={4}>{nameValuePair.value}</Col>
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

