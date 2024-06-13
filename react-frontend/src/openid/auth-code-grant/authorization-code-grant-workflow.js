import React, {useState} from "react";
import Col from "react-bootstrap/Col";
import {AlertListMessages, Collapseable, DpopDetails, LoadingSpinner} from "../../base/form-base";
import Row from "react-bootstrap/Row";
import {CaretDown, CaretRight, ExclamationLg, XLg} from "react-bootstrap-icons";
import {Alert, Card, Collapse, FormCheck} from "react-bootstrap";
import ScimClient from "../../scim/scim-client";
import {
  ACCESS_TOKEN_REQUEST_ENDPOINT,
  AUTH_CODE_GRANT_ENDPOINT,
  CURRENT_WORKFLOW_SETTINGS_ENDPOINT,
  CURRENT_WORKFLOW_URI
} from "../../scim/scim-constants";
import Button from "react-bootstrap/Button";
import {AccessTokenDetailsView} from "./access-token-view";
import {GoFlame} from "react-icons/go";
import {Optional} from "../../services/utils";
import {ResourceEndpointDetailsView} from "./resource-endpoint-view";
import {AiFillWarning} from "react-icons/ai";

const INTERVAL_TIME_IN_SECONDS = 2;
const MAX_GET_AUTHCODE_RETRIES = 60;

export default class AuthorizationCodeGrantWorkflow extends React.Component
{
  constructor(props)
  {
    super(props);

    let previousDpopKeyId = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.keyId;
    let previousDpopSignatureAlgorithm = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.signatureAlgorithm;
    let previousDpopNonce = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.nonce;
    let previousDpopJti = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.jti;
    let previousDpopHtm = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.htm;
    let previousDpopHtu = props.requestDetails[CURRENT_WORKFLOW_URI]?.dpop?.htu;

    this.state = {
      isLoading: true,
      responses: [],
      dpopKeyId: previousDpopKeyId || ((props.keyInfos || []).length > 0 ? props.keyInfos[0].alias : null),
      dpopSignatureAlgorithm: previousDpopSignatureAlgorithm ||
        ((props.appInfo.jwtInfo.signatureAlgorithms || []).length > 0
          ? props.appInfo.jwtInfo.signatureAlgorithms[0] : null),
      dpopNonce: previousDpopNonce || "",
      dpopJti: previousDpopJti || "",
      dpopHtm: previousDpopHtm || "",
      dpopHtu: previousDpopHtu || "",
    };
    this.setState = this.setState.bind(this);
    this.forceUpdate = this.forceUpdate.bind(this);
    this.loadPushedAuthorizationRequestView = this.loadPushedAuthorizationRequestView.bind(this);
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
      if (this.openedWindow)
      {
        this.openedWindow.close();
      }
    }
  }

  componentDidMount()
  {
    let getAuthRequestStatus = this.getAuthRequestStatus;
    let browserWindow = window.open(this.props.requestDetails.authorizationCodeGrantUrl,
      '_blank',
      'location=yes,height=570,width=520,scrollbars=yes,status=yes');
    let requestCounter = 0;
    let state = this.state;
    let setState = this.setState;
    this.openedWindow = browserWindow;

    this.state.interval = setInterval(function ()
    {
      getAuthRequestStatus();
      requestCounter++;
      if (browserWindow.closed || requestCounter >= MAX_GET_AUTHCODE_RETRIES)
      {
        clearInterval(state.interval);
        browserWindow.close();
        setState({isLoading: false});
        if (browserWindow.closed)
        {
          setState({windowClosed: true});
        } else
        {
          setState({maxRetriesExceeded: true});
        }
      }
    }, INTERVAL_TIME_IN_SECONDS * 1000);
  }

  getAuthRequestStatus()
  {
    let scimClient = new ScimClient(AUTH_CODE_GRANT_ENDPOINT, this.setState);
    let authCodeQueryParams = Object.fromEntries(
      new URL("http://localhost?" + this.props.requestDetails.authorizationCodeGrantParameters).searchParams);
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
          delete state.interval;

          setState({
            authorizationResponseUrl: resource.authorizationResponseUrl,
            isLoading: resource.authorizationResponseUrl === undefined
          });
        });
      }
    });
  }

  loadPushedAuthorizationRequestView()
  {
    return <div className={"workflow-details mb-2"}>
      <Collapseable header={"Pushed Authorization Request Details"} variant={"workflow-details"}
                    bodyClass={"workflow-card-details"}
                    content={() =>
                    {
                      let parEndpoint = this.props.requestDetails.metaDataJson.pushed_authorization_request_endpoint;
                      if (!parEndpoint || !this.props.requestDetails?.authorizationCodeGrantParameters)
                      {
                        return null;
                      }

                      let queryParameters = this.props.requestDetails.authorizationCodeGrantParameters;
                      let fullUrl = parEndpoint + '?' + queryParameters;

                      let authCodeQueryParams = Object.fromEntries(new URL(fullUrl).searchParams);

                      return <React.Fragment>
                        <Row>
                          <Col sm={2} className={"url-base-value"}>HTTP method</Col>
                          <Col sm={10}
                               className={"url-base-value"}>POST</Col>
                        </Row>
                        <Row>
                          <Col sm={2} className={"url-base-value"}>authCodeUrl</Col>
                          <Col sm={10}
                               className={"url-base-value"}>{parEndpoint}</Col>
                        </Row>
                        <Row>
                          <Col sm={2} className={"url-base-value"}>request body</Col>
                          <Col sm={10}
                               className={"url-base-value"}>{queryParameters}</Col>
                        </Row>
                        {
                          Object.keys(authCodeQueryParams).map((key, index) =>
                          {
                            return <Row key={"auth-code-request-row-" + index}>
                              <Col sm={2}>{key}</Col>
                              <Col sm={10}>{authCodeQueryParams[key]}</Col>
                            </Row>;
                          })
                        }
                      </React.Fragment>;
                    }}/>
    </div>
  }

  loadPushedAuthorizationResponseView()
  {
    let formattedJson = JSON.stringify(JSON.parse(this.props.requestDetails.pushedAuthorizationResponse), null, 2);

    return <div className={"workflow-details"}>
      <Collapseable header={"Pushed Authorization Request Details"} variant={"workflow-details"}
                    bodyClass={"workflow-card-details"}
                    content={() =>
                    {
                      return <React.Fragment>
                        <Row>
                          <Col sm={2} className={"url-base-value"}>Response Body</Col>
                          <Col sm={10} className={"url-base-value"}>
                            <pre className={"mb-0"}>
                              {formattedJson}
                            </pre>
                          </Col>
                        </Row>
                      </React.Fragment>;
                    }}/>
    </div>
  }

  loadAuthorizationQueryParameterView()
  {
    let authCodeQueryParams = Object.fromEntries(
      new URL(this.props.requestDetails.authorizationCodeGrantUrl).searchParams);

    let showInfoMessage = this.state.authorizationResponseUrl === undefined;

    return <div className={"workflow-details mb-2"}>
      <Alert variant={"info"} show={showInfoMessage}>
        <ExclamationLg/> The authorization code grant will try to open a new browser window. Make sure your
                         popup blocker does not block this. If you closed this window before finishing the
                         login process. Close this workflow and start again.
      </Alert>
      <Collapseable header={"Authorization Request Details"} variant={"workflow-details"}
                    bodyClass={"workflow-card-details"}
                    content={() =>
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
                            </Row>;
                          })
                        }
                      </React.Fragment>;
                    }}/>
    </div>;
  }

  retrieveAccessTokenDetails(e)
  {
    e.preventDefault();
    this.setState({errors: null})
    let openIdClientId = this.props.requestDetails[CURRENT_WORKFLOW_URI].openIdClientId;
    {
      let workflowSettingsPatchResource = {
        schemas: [CURRENT_WORKFLOW_URI],
        dpop: {
          keyId: this.state.dpopKeyId,
          signatureAlgorithm: this.state.dpopSignatureAlgorithm,
          nonce: this.state.dpopNonce,
          jti: this.state.dpopJti,
          htm: this.state.dpopHtm,
          htu: this.state.dpopHtu
        }
      }
      let patchRequest = {
        schemas: ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
        Operations: [
          {
            op: "replace",
            value: workflowSettingsPatchResource
          }
        ]
      }
      let scimClient = new ScimClient(CURRENT_WORKFLOW_SETTINGS_ENDPOINT, this.setState);
      scimClient.patchResource(patchRequest, openIdClientId).then(response =>
      {
        if (response.success)
        {
          response.resource.then(resource =>
          {
            this.props.requestDetails[CURRENT_WORKFLOW_URI] = resource;
          });
        }
      })
    }

    let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

    let authorizationResponseUrl = new URL(this.state.authorizationResponseUrl);
    const queryParamsObject = Object.fromEntries(authorizationResponseUrl.searchParams);
    let authCodeQueryParams = Object.fromEntries(
      new URL(this.props.requestDetails.authorizationCodeGrantUrl
        + "?" + this.props.requestDetails.authorizationCodeGrantParameters).searchParams);

    let resource = {
      grantType: "authorization_code",
      openIdClientId: parseInt(this.props.client.id),
      redirectUri: authCodeQueryParams.redirect_uri,
      authorizationCode: queryParamsObject.code,
      state: queryParamsObject.state
    };

    if (this.state.useDpop)
    {
      resource[CURRENT_WORKFLOW_URI] = {
        openIdClientId: openIdClientId
      }
      resource[CURRENT_WORKFLOW_URI].dpop = {
        useDpop: true,
        keyId: this.state.dpopKeyId,
        signatureAlgorithm: this.state.dpopSignatureAlgorithm,
        nonce: this.state.dpopNonce,
        jti: this.state.dpopJti,
        htm: this.state.dpopHtm,
        htu: this.state.dpopHtu,
      }
    }

    scimClient.createResource(resource).then(response =>
    {
      if (response.success)
      {
        response.resource.then(resource =>
        {
          this.setState({accessTokenDetails: resource});
        });
      } else
      {
        response.resource.then(errorResponse =>
        {
          this.setState({errors: errorResponse})
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
                        bodyClass={"workflow-card-details"}
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
                                </Row>;
                              })
                            }
                          </React.Fragment>;
                        }}
          />
          <AlertListMessages variant={"danger"} icon={<GoFlame/>}
                             messages={errors.errorMessages || new Optional(errors.detail).map(d => [d])
                               .orElse(
                                 [])}
                             onClose={() => this.setState({errors: null})}/>
          <Row>
            <Col>
              <Button type="submit" onClick={async e =>
              {
                await this.setState({
                  isLoading: true
                });
                this.retrieveAccessTokenDetails(e);
              }}
                      style={{
                        marginTop: "15px",
                        marginBottom: "15px"
                      }}>
                <LoadingSpinner show={this.state.isLoading}/> Get Access Token
              </Button>
              <FormCheck
                type="switch"
                id="dpop-switch"
                className={"ms-5 d-inline-block"}
                label="Demonstrate Proof of Posession (DPoP)"
                onChange={e =>
                {
                  this.setState({useDpop: e.target.checked})
                }}/>
            </Col>
            {
              this.state.useDpop &&
              <Col md={7}>
                <DpopDetails props={this.props} state={this.state} setState={this.setState}/>
              </Col>
            }
          </Row>
          {
            this.state.accessTokenDetails &&
            <AccessTokenDetailsView key={this.state.accessTokenDetails.id}
                                    accessTokenDetails={this.state.accessTokenDetails}/>
          }

          {
            new Optional(this.state.accessTokenDetails).map(details => JSON.parse(details.plainResponse))
              .map(json => json.access_token).isPresent() &&
            <Collapseable header={"Access Resource Endpoints"}
                          variant={"workflow-details"}
                          headerClass={"nested-workflow-details-header"}
                          bodyClass={"nested-workflow-details-body"}
                          open={true}
                          content={() => <ResourceEndpointDetailsView
                            metaData={this.props.requestDetails.metaDataJson}
                            accessTokenDetails={this.state.accessTokenDetails}/>}/>
          }

        </React.Fragment>
      }
    </div>;
  }

  render()
  {
    let isLoading = !this.openedWindow?.closed;

    return (
      <div className={"grant-type-workflow"}>
        <AuthorizationCodeGrantDetails
          isLoading={isLoading}
          content={() =>
          {
            return <React.Fragment>
              {
                this.state.maxRetriesExceeded &&
                <Alert variant={"warning"} show={true}>
                  <AiFillWarning/>
                  Was not able to retrieve the AuthorizationCode in due time. The PopUp was closed automatically.
                </Alert>
              }
              {
                this.state.windowClosed && this.state.authorizationResponseUrl === undefined &&
                <Alert variant={"warning"} show={true}>
                  <AiFillWarning/>
                  PopUp closed, process stopped
                </Alert>
              }
              <Collapseable header={"OpenID Connect Discovery Details"}
                            headerClass={"mb-2"}
                            variant={"workflow-details"}
                            bodyClass={"workflow-card-details"}
                            content={() =>
                            {
                              return <pre>
                                        {JSON.stringify(this.props.requestDetails.metaDataJson,
                                          undefined,
                                          2)}
                                     </pre>;
                            }}/>

              <hr/>
              {
                "PUSHED_AUTHORIZATION_CODE" === this.props.requestDetails.authenticationType &&
                <>
                  {this.loadPushedAuthorizationRequestView()}
                  {this.loadPushedAuthorizationResponseView()}
                  <hr/>
                </>
              }
              {this.loadAuthorizationQueryParameterView()}
              {this.loadAuthorizationCodeResponseDetailsView()}
            </React.Fragment>;
          }}
          remove={this.props.onRemove}/>
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
          <CaretDown/>
        }
        {
          open === false &&
          <CaretRight/>
        }
        <span><LoadingSpinner show={props.isLoading}/> Authorization Code Grant/Flow</span>
        {
          props.remove !== undefined &&
          <XLg onClick={props.remove} className={"remove-collapse"}/>
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


