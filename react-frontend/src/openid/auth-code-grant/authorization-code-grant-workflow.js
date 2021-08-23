import React, {useState} from "react";
import Col from "react-bootstrap/Col";
import {AlertListMessages, Collapseable, LoadingSpinner} from "../../base/form-base";
import Row from "react-bootstrap/Row";
import {CaretDown, CaretRight, ExclamationLg, XLg} from "react-bootstrap-icons";
import {Alert, Card, Collapse} from "react-bootstrap";
import ScimClient from "../../scim/scim-client";
import {ACCESS_TOKEN_REQUEST_ENDPOINT, AUTH_CODE_GRANT_ENDPOINT} from "../../scim/scim-constants";
import Button from "react-bootstrap/Button";
import {AccessTokenDetailsView} from "./access-token-view";
import {GoFlame} from "react-icons/go";
import {Optional} from "../../services/utils";

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
        this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
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
        let getAuthRequestStatus = this.getAuthRequestStatus;
        window.open(this.props.requestDetails.authorizationCodeGrantUrl,
            '_blank',
            'location=yes,height=570,width=520,scrollbars=yes,status=yes');
        this.state.interval = setInterval(function ()
        {
            getAuthRequestStatus();
        }, 2000);
    }

    getAuthRequestStatus()
    {
        let scimClient = new ScimClient(AUTH_CODE_GRANT_ENDPOINT, this.setState);
        let authCodeQueryParams = Object.fromEntries(
            new URL(this.props.requestDetails.authorizationCodeGrantUrl).searchParams);
        let stateParam = authCodeQueryParams.state;

        let state = this.state;
        let setState = this.setState;

        scimClient.getResource(stateParam).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    clearInterval(state.interval);
                    delete state.interval
                    setState({
                        authorizationResponseUrl: resource.authorizationResponseUrl
                    });
                })
            }
        });
    }

    loadAuthorizationQueryParameterView()
    {
        let authCodeQueryParams = Object.fromEntries(
            new URL(this.props.requestDetails.authorizationCodeGrantUrl).searchParams);

        let showInfoMessage = this.state.authorizationResponseUrl === undefined;

        return <div className={"workflow-details"}>
            <Alert variant={"info"} show={showInfoMessage}>
                <ExclamationLg /> The authorization code grant will try to open a new browser window. Make sure your
                                  popup blocker does not block this. If you closed this window before finishing the
                                  login process. Close this workflow and start again.
            </Alert>
            <Collapseable header={"Authorization Request Details"} variant={"workflow-details"} content={() =>
            {
                return <React.Fragment>
                    <Row>
                        <Col sm={2} className={"url-base-value"}>authCodeUrl</Col>
                        <Col sm={10}
                             className={"url-base-value"}>{this.props.requestDetails.authorizationCodeGrantUrl}</Col>
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

    retrieveAccessTokenDetails(e)
    {
        e.preventDefault();
        let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

        let authorizationResponseUrl = new URL(this.state.authorizationResponseUrl);
        const queryParamsObject = Object.fromEntries(authorizationResponseUrl.searchParams);
        let authCodeQueryParams = Object.fromEntries(
            new URL(this.props.requestDetails.authorizationCodeGrantUrl).searchParams);

        let resource = {
            grantType: "authorization_code",
            openIdClientId: parseInt(this.props.client.id),
            redirectUri: authCodeQueryParams.redirect_uri,
            authorizationCode: queryParamsObject.code
        }
        scimClient.createResource(resource).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    this.setState({accessTokenDetails: resource})
                })
            }
        });
    }

    loadAuthorizationCodeResponseDetailsView()
    {
        if (!this.state.authorizationResponseUrl)
        {
            return null;
        }
        let authorizationResponseUrl = new URL(this.state.authorizationResponseUrl);
        const queryParamsObject = Object.fromEntries(authorizationResponseUrl.searchParams);

        let errors = this.state.errors || {};
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

                    <Button type="submit" onClick={e =>
                    {
                        this.setState({isLoading: true});
                        this.retrieveAccessTokenDetails(e);
                    }}
                            style={{marginTop: "15px", marginBottom: "15px"}}>
                        <LoadingSpinner show={this.state.isLoading} /> Get Access Token
                    </Button>
                    {
                        this.state.accessTokenDetails &&
                        <AccessTokenDetailsView accessTokenDetails={this.state.accessTokenDetails} />
                    }

                    <AlertListMessages variant={"danger"} icon={<GoFlame />}
                                       messages={errors.errorMessages || new Optional(errors.detail).map(d => [d])
                                                                                                    .orElse(
                                                                                                        [])} />
                </React.Fragment>
            }
        </div>
    }

    render()
    {
        return (
            <div className={"grant-type-workflow"}>
                {
                    <AuthorizationCodeGrantDetails
                        isLoading={this.state.authorizationResponseUrl === undefined}
                        content={() =>
                        {
                            return <React.Fragment>
                                {this.loadAuthorizationQueryParameterView()}
                                {this.loadAuthorizationCodeResponseDetailsView()}
                            </React.Fragment>
                        }}
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
            <Alert className={"collapse-header authorization-code-grant"}
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
                <span><LoadingSpinner show={props.isLoading} /> Authorization Code Grant/Flow</span>
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


