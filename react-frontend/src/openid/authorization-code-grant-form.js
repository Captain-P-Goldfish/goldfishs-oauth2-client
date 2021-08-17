import React, {useState} from "react";
import Col from "react-bootstrap/Col";
import {Collapseable, ErrorListItem, FormInputField, LoadingSpinner} from "../base/form-base";
import Row from "react-bootstrap/Row";
import {Optional, parseJws} from "../services/utils";
import Button from "react-bootstrap/Button";
import {CaretDown, CaretRight, Reply, XLg} from "react-bootstrap-icons";
import Form from "react-bootstrap/Form";
import {Alert, Card, Collapse} from "react-bootstrap";
import Jws from "../jwt/jws";

export default class AuthorizationCodeGrantForm extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            intervals: [],
            requestDetails: []
        }
        this.setState = this.setState.bind(this);
        this.forceUpdate = this.forceUpdate.bind(this);
        this.loadAuthorizationCode = this.loadAuthorizationCode.bind(this);
        this.loadAuthorizationRequestDetails = this.loadAuthorizationRequestDetails.bind(this);
        this.loadAuthorizationQueryParameterView = this.loadAuthorizationQueryParameterView.bind(this);
        this.loadAuthorizationDetailsArray = this.loadAuthorizationDetailsArray.bind(this);
        this.pullAuthorizationCode = this.pullAuthorizationCode.bind(this);
        this.getAuthRequestStatus = this.getAuthRequestStatus.bind(this);
        this.loadAuthorizationCodeResponseDetailsView = this.loadAuthorizationCodeResponseDetailsView.bind(this);
    }

    componentWillUnmount()
    {
        this.state.requestDetails.forEach(requestDetails =>
        {
            if (requestDetails.interval !== undefined)
            {
                clearInterval(requestDetails.interval);
            }
        })
    }

    async loadAuthorizationCode(e)
    {
        e.preventDefault();
        let details = await this.loadAuthorizationRequestDetails();
        // window.open(details.authCodeUrl, '_blank', 'width=800,height=600');
        this.setState({requestDetails: [...this.state.requestDetails, details]}) //simple value
        this.pullAuthorizationCode(details);
    }

    async loadAuthorizationRequestDetails()
    {
        return {
            backendToken: Math.random().toString(36).substring(2, 15),
            authCodeUrl: "http://localhost:8080/auth/realms/master/...",
            queryParameters: [
                {
                    name: "client_id",
                    value: "goldfish"
                },
                {
                    name: "redirect_uri",
                    value: "http://localhost"
                },
                {
                    name: "state",
                    value: Math.random().toString(36).substring(2, 15)
                },
                {
                    name: "grant_type",
                    value: "authorization_code"
                },
            ]
        }
    }

    pullAuthorizationCode(details)
    {
        let forceUpdate = this.forceUpdate
        let getAuthRequestStatus = this.getAuthRequestStatus;
        details.interval = setInterval(function ()
        {
            let authRequestStatus = getAuthRequestStatus();
            if (authRequestStatus.success)
            {
                clearInterval(details.interval);
                delete details.interval;
                details.authResponseParameters = authRequestStatus.authResponseParameters;
                forceUpdate();
            }
        }, 200);
    }

    getAuthRequestStatus()
    {
        return {
            success: true,
            authResponseParameters: [
                {
                    name: "code",
                    value: Math.random().toString(36).substring(2, 15)
                },
                {
                    name: "state",
                    value: Math.random().toString(36).substring(2, 15)
                },
            ]
        };
    }

    loadAuthorizationDetailsArray()
    {
        return (
            <React.Fragment>
                {
                    this.state.requestDetails.length > 0 &&
                    <Col sm={{span: 10, offset: 2}}>
                        {
                            this.state.requestDetails.map((details, index) =>
                            {
                                return <AuthorizationRequestDetails
                                    key={"auth-request-details-" + details.backendToken}
                                    content={() =>
                                    {
                                        return <React.Fragment>
                                            {this.loadAuthorizationQueryParameterView(details)}
                                            {this.loadAuthorizationCodeResponseDetailsView(details)}
                                        </React.Fragment>
                                    }}
                                    details={details}
                                    remove={() =>
                                    {
                                        let requestDetails = this.state.requestDetails;
                                        let indexOfDetails = this.state.requestDetails.indexOf(details)
                                        requestDetails.splice(indexOfDetails, 1)
                                        this.setState({requestDetails: requestDetails})
                                    }} />
                            })
                        }
                    </Col>
                }
                <AccessTokenResponse contentType={"application/json;encoding=utf-8"}
                                     tokenResponse={"{\n"
                                                    + "  \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c\",\n"
                                                    + "  \"token_type\": \"Bearer\",\n"
                                                    + "  \"expires_in\": 3600,\n"
                                                    + "  \"refresh_token\": \"cba\"\n"
                                                    + "}"} />
            </React.Fragment>
        )
    }

    loadAuthorizationQueryParameterView(details)
    {
        return <Collapseable header={"Authorization Request Details"} content={() =>
        {
            return <React.Fragment>
                <Row>
                    <Col sm={2}>authCodeUrl</Col>
                    <Col>{details.authCodeUrl}</Col>
                </Row>
                {
                    details.queryParameters.map((queryObject, index) =>
                    {
                        return <Row key={"auth-code-request-row-" + index}>
                            <Col sm={2}>{queryObject.name}</Col>
                            <Col sm={4}>{queryObject.value}</Col>
                        </Row>
                    })
                }
            </React.Fragment>
        }} />
    }

    loadAuthorizationCodeResponseDetailsView(details)
    {
        return <React.Fragment>
            {
                new Optional(details.authResponseParameters).isPresent() &&
                <React.Fragment>
                    <Collapseable header={"Authorization Response Details"}
                                  content={() =>
                                  {
                                      return details.authResponseParameters.map((authResponseParam, index) =>
                                      {
                                          return <Row key={"auth-code-response-row-" + index}>
                                              <Col sm={2}>{authResponseParam.name}</Col>
                                              <Col sm={4}>{authResponseParam.value}</Col>
                                          </Row>
                                      })
                                  }}
                    />
                    <AccessTokenView />
                </React.Fragment>
            }
        </React.Fragment>
    }

    render()
    {
        return (
            <React.Fragment>
                <FormInputField name="redirectUri"
                                label="Redirect URI"
                                value={this.props.redirectUri}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)}>
                    <a href={"#"} onClick={this.props.resetRedirectUri} className={"action-link"}>
                        <Reply /> <span>reset redirect uri</span>
                    </a>
                </FormInputField>
                <FormInputField name="urlParameters"
                                label="URL Parameter"
                                placeholder="add an optional query string that is appended to the request URL"
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)} />
                <Form.Group as={Row}>
                    <Col sm={{span: 10, offset: 2}}>
                        <Button type="submit" onClick={this.loadAuthorizationCode}>
                            <LoadingSpinner show={this.props.isLoading} /> Get Authorization Code
                        </Button>
                    </Col>
                </Form.Group>
                {this.loadAuthorizationDetailsArray()}
            </React.Fragment>
        );
    }
}

function AuthorizationRequestDetails(props)
{
    const [open, setOpen] = useState(false);
    const [onceOpenend, setOnceOpenend] = useState(false);
    let authorizationCode = new Optional(props.details.authResponseParameters)
        .map(params => params.filter(param => param.name === "code"))
        .map(param => param[0].value);
    let codePresent = authorizationCode.isPresent();
    let codeSeen = codePresent && onceOpenend === true;
    let variant = codePresent === false ? "danger" : (codeSeen === true ? "success" : "primary");
    return (
        <React.Fragment>
            <Alert className={"collapse-header"}
                   variant={variant}
                   onClick={() =>
                   {
                       setOpen(!open);
                       setOnceOpenend(true);
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
                Authorization Details
                {
                    authorizationCode.isPresent() &&
                    <React.Fragment>
                        <span className={"bold"}> - Authorization Code: </span>{authorizationCode.get()}
                    </React.Fragment>
                }
                {
                    props.remove !== undefined &&
                    <XLg onClick={props.remove} className={"remove-collapse"} />
                }
            </Alert>
            <Collapse in={open}>
                <Card>
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
                              content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                            nameValueList={(((state.accessTokenDetails
                                                                            || {}).requestDetails || {}).requestHeaders
                                                                            || [])} />} />
                <Collapseable header={"Request Parameter"}
                              content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                            nameValueList={(((state.accessTokenDetails
                                                                            || {}).requestDetails || {}).requestParams
                                                                            || [])} />} />
            </React.Fragment>
        }
        return <Collapseable header={"Access Token Request Details"}
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
                              content={() => <NameValueList keyPrefix={"access-token-request-header-row-"}
                                                            nameValueList={responseHeaders} />} />
                <Collapseable header={"Access Token Response"}
                              content={() => <AccessTokenResponse contentType={contentType}
                                                                  tokenResponse={plainResponse} />} />
            </React.Fragment>
        }
        return <Collapseable header={header()}
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

    parseJsonContent()
    {
        let isContentTypeJson = (this.props.contentType || "").toLowerCase().includes("application/json");
        try
        {
            this.setState({json: JSON.parse(this.props.tokenResponse)});
            if (!isContentTypeJson)
            {
                this.setState({
                    errorMessages: [...this.state.errorMessages,
                                    "Found invalid content-type: " + this.props.contentType + ". Response is valid "
                                    + "JSON but content-type header does not match."]
                })
            }
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
                            <TokenValueColumn value={this.state.json[key]} />
                        </Row>
                    })
                }
                {
                    !this.state.json &&
                    <p>{this.props.tokenResponse}</p>
                }
            </div>
        )
    }
}

function TokenValueColumn(props)
{
    let jwt = parseJws(props.value);
    let sm = props.sm || 10;
    if (jwt === null)
    {
        return <Col sm={sm}>
            {props.value}
        </Col>
    }
    return <Jws sm={sm}>
        {JSON.stringify(jwt)}
    </Jws>

}

