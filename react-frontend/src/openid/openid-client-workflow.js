import React, {createRef, useContext} from "react";
import Form from "react-bootstrap/Form";
import {
  AlertListMessages,
  DpopDetails,
  ErrorMessagesAlert,
  FormInputField,
  FormRadioSelection,
  LoadingSpinner
} from "../base/form-base";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {Reply} from "react-bootstrap-icons";
import AuthorizationCodeGrantWorkflow from "./auth-code-grant/authorization-code-grant-workflow";
import * as lodash from "lodash";
import ScimClient from "../scim/scim-client";
import {
  ACCESS_TOKEN_REQUEST_ENDPOINT,
  AUTH_CODE_GRANT_ENDPOINT,
  CURRENT_WORKFLOW_URI,
  KEYSTORE_ENDPOINT
} from "../scim/scim-constants";
import AccessTokenView from "./auth-code-grant/access-token-view";
import {GoFlame} from "react-icons/go";
import {Optional} from "../services/utils";
import CurrentWorkflowSettingsClient from "../scim/current-workflow-settings-client";
import {Alert, FormCheck, FormText} from "react-bootstrap";
import {value} from "lodash/seq";
import {ApplicationInfoContext} from "../app";

export default class OpenidClientWorkflow extends React.Component
{

  constructor(props, context)
  {
    super(props, context)
    this.authCodeGrantType = "authorization_code";
    this.clientCredentialsGrantType = "client_credentials";
    this.resourceOwnerGrantType = "password";

    this.state = {
      authenticationType: this.authCodeGrantType,
      workflowDetails: this.props.client[CURRENT_WORKFLOW_URI] || {},
      isLoading: false,
      responseDetails: []
    }

    this.formReference = createRef();
    this.setState = this.setState.bind(this);
    this.resetRedirectUri = this.resetRedirectUri.bind(this);
    this.handleNestedElementChange = this.handleNestedElementChange.bind(this);
    this.handleGrantTypeResponseDetails = this.handleGrantTypeResponseDetails.bind(this);
    this.removeGrantTypeDetails = this.removeGrantTypeDetails.bind(this);
  }


  componentDidMount()
  {
    // load private keys for DPoP
    {
      new ScimClient(KEYSTORE_ENDPOINT, this.setState).getResource(null).then(response =>
      {
        if (response.success)
        {
          response.resource.then(listResponse =>
          {
            this.setState({keyInfos: listResponse.Resources[0].keyInfos});
          })
        }
      });
    }
  }

  async resetRedirectUri(e)
  {
    e.preventDefault();
    let wrapperObject = this.state;
    lodash.set(wrapperObject, "workflowDetails.authCodeParameters.redirectUri", this.props.originalRedirectUri);
    this.setState(wrapperObject)
  }

  handleGrantTypeResponseDetails(type, responseDetails)
  {
    let responseDetailsArray = (this.state.responseDetails || []);
    responseDetails.grantType = type;
    responseDetailsArray.unshift(responseDetails);
    let wrapperObject = {};
    wrapperObject.responseDetails = responseDetailsArray
    let metaData = new Optional(responseDetails.metaDataJson).orElse(this.state.metaData)
    this.setState({responseDetails: responseDetailsArray, metaData: metaData})
  }

  handleNestedElementChange(fieldname, value)
  {
    let wrapperObject = this.state.workflowDetails;
    lodash.set(wrapperObject, fieldname, value);
    this.setState({workflowDetails: wrapperObject})
  }

  removeGrantTypeDetails(details)
  {
    let detailsObject = this.state.responseDetails;
    let detailsIndex = detailsObject.indexOf(details);
    detailsObject.splice(detailsIndex, 1);
    let wrapperObject = {};
    wrapperObject.responseDetails = detailsObject;
    this.setState(wrapperObject)
  }

  render()
  {
    let authTypes = [
      {value: this.authCodeGrantType, display: "Authorization Code Grant/Flow"},
      {value: this.clientCredentialsGrantType, display: "Client Credentials Grant"},
      {value: this.resourceOwnerGrantType, display: "Resource Owner Password Credentials Grant"}
    ]

    return (
      <React.Fragment>
        <h2>OpenID Connect Workflow</h2>
        <ErrorMessagesAlert errors={this.state.errors}/>

        <Form ref={this.formReference} onSubmit={e => e.preventDefault()}>
          <FormRadioSelection name="authenticationType"
                              label="AuthenticationType Type"
                              displayType={"vertical"}
                              selected={this.state.authenticationType}
                              selections={authTypes}
                              onChange={e => this.setState({authenticationType: e.target.value})}
                              onError={() =>
                              {
                              }}/>
          {
            this.state.authenticationType === this.authCodeGrantType &&
            <AuthorizationCodeGrantForm formReference={this.formReference}
                                        originalRedirectUri={this.props.originalRedirectUri}
                                        workflowDetails={this.state.workflowDetails}
                                        isLoading={this.state.isLoading}
                                        handleChange={this.handleNestedElementChange}
                                        resetRedirectUri={this.resetRedirectUri}
                                        handleResponse={details => this.handleGrantTypeResponseDetails(
                                          this.authCodeGrantType, details)}
                                        onError={() =>
                                        {
                                        }}/>
          }
          <ApplicationInfoContext.Consumer>
            {appInfo =>
              appInfo &&
              <>
                {
                  this.state.authenticationType === this.clientCredentialsGrantType &&
                  <ClientCredentialsGrantForm formReference={this.formReference}
                                              appInfo={appInfo}
                                              workflowDetails={this.state.workflowDetails}
                                              client={this.props.client}
                                              keyInfos={this.state.keyInfos}
                                              isLoading={this.state.isLoading}
                                              handleChange={this.handleNestedElementChange}
                                              handleResponse={details => this.handleGrantTypeResponseDetails(
                                                this.clientCredentialsGrantType, details)}
                                              onError={() =>
                                              {
                                              }}/>
                }
                {
                  this.state.authenticationType === this.resourceOwnerGrantType &&
                  <ResourceOwnerPasswordCredentialsForm
                    client={this.props.client}
                    appInfo={appInfo}
                    keyInfos={this.state.keyInfos}
                    workflowDetails={this.state.workflowDetails}
                    isLoading={this.state.isLoading}
                    handleChange={this.handleNestedElementChange}
                    handleResponse={details => this.handleGrantTypeResponseDetails(
                      this.state.authenticationType, details)}
                    onError={() =>
                    {
                    }}/>
                }
              </>
            }
          </ApplicationInfoContext.Consumer>
        </Form>
        {
          (this.state.responseDetails || []).map(responseDetails =>
          {
            return <ResponseDetailsView key={"response-details-" + responseDetails.id}
                                        responseDetails={responseDetails}
                                        keyInfos={this.state.keyInfos}
                                        client={this.props.client}
                                        metaData={this.state.metaData}
                                        removeGrantTypeDetails={this.removeGrantTypeDetails}/>
          })
        }
      </React.Fragment>
    )
  }
}

function ResponseDetailsView(props)
{
  const applicationInfoContext = useContext(ApplicationInfoContext);
  let authCodeGrantType = "authorization_code";
  let clientCredentialsGrantType = "client_credentials";
  let resourceOwnerGrantType = "password";

  let responseDetails = props.responseDetails;

  if (authCodeGrantType === responseDetails.grantType)
  {
    return <React.Fragment key={authCodeGrantType + "-" + responseDetails.id}>
      <AuthorizationCodeGrantWorkflow client={props.client}
                                      appInfo={applicationInfoContext}
                                      keyInfos={props.keyInfos}
                                      requestDetails={responseDetails}
                                      onRemove={() => props.removeGrantTypeDetails(
                                        responseDetails)}/>
    </React.Fragment>
  }

  if (clientCredentialsGrantType === responseDetails.grantType)
  {
    return <React.Fragment key={clientCredentialsGrantType + "-" + responseDetails.id}>
      <AccessTokenView header={"Client Credentials Grant"}
                       headerClass={"client-credentials-grant"}
                       metaData={props.metaData}
                       accessTokenDetails={responseDetails}
                       onRemove={() => props.removeGrantTypeDetails(
                         responseDetails)}/>
    </React.Fragment>
  }

  return <React.Fragment key={resourceOwnerGrantType + "-" + responseDetails.id}>
    <AccessTokenView header={"Resource Owner Password Credentials Grant"}
                     headerClass={"resource-owner-password-credentials-grant"}
                     metaData={props.metaData}
                     accessTokenDetails={responseDetails}
                     onRemove={() => props.removeGrantTypeDetails(
                       responseDetails)}/>
  </React.Fragment>
}

class AuthorizationCodeGrantForm extends React.Component
{
  constructor(props)
  {
    super(props);
    this.state = {
      intervals: [],
      requestDetails: [],
      pkce: props.workflowDetails.pkce,
      pkceCodeVerifierLength: props.workflowDetails.pkce?.codeVerifier?.length || 0
    }
    this.setState = this.setState.bind(this);
    this.forceUpdate = this.forceUpdate.bind(this);
    this.loadAuthorizationRequestDetails = this.loadAuthorizationRequestDetails.bind(this);
    this.patchWorkflowSettings = this.patchWorkflowSettings.bind(this);
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

  async loadAuthorizationRequestDetails(e)
  {
    e.preventDefault();
    this.patchWorkflowSettings();
    this.setState({getAuthcode: true, errorMessage: undefined});

    let scimClient = new ScimClient(AUTH_CODE_GRANT_ENDPOINT, this.setState);
    let resource = await scimClient.getResourceFromFormReference(this.props.formReference);
    resource[CURRENT_WORKFLOW_URI] = this.props.workflowDetails;
    lodash.set(resource[CURRENT_WORKFLOW_URI], "authCodeParameters.redirectUri", resource.authCodeParameters.redirectUri)

    let handleResponse = this.props.handleResponse;
    scimClient.createResource(resource).then(response =>
    {
      this.setState({getAuthcode: false});
      if (response.success)
      {
        response.resource.then(resource =>
        {
          handleResponse(resource);
        })
      } else
      {
        response.resource.then(errorResponse =>
        {
          this.setState({errorMessage: errorResponse.detail});
        })
      }
    });
  }

  patchWorkflowSettings()
  {
    let workflowSettingsClient = new CurrentWorkflowSettingsClient(this.setState);

    let openIdClientId = new Optional(this.props.workflowDetails).map(w => w.openIdClientId)
      .orElse(undefined);
    let redirectUri = new Optional(this.props.workflowDetails).map(w => w.authCodeParameters)
      .map(a => a.redirectUri)
      .orElse(this.props.originalRedirectUri);
    let queryParams = new Optional(this.props.workflowDetails).map(w => w.authCodeParameters)
      .map(a => a.queryParameters)
      .orElse(undefined);
    let usePkce = new Optional(this.state.pkce).map(a => a.use).orElse(undefined);
    let pkceCodeVerifier = new Optional(this.state.pkce).map(a => a.codeVerifier).orElse(undefined);
    workflowSettingsClient.updateAuthCodeSettings(openIdClientId, redirectUri, usePkce, pkceCodeVerifier, queryParams);
  }

  render()
  {
    let authCodeParameters = this.props.workflowDetails.authCodeParameters
      || {redirectUri: this.props.originalRedirectUri};

    return (
      <React.Fragment>
        <FormInputField name="authCodeParameters.redirectUri"
                        label="Redirect URI"
                        placeholder="The redirect uri that is added to the request parameters"
                        value={authCodeParameters.redirectUri}
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}>
          <a href={"/#"} onClick={this.props.resetRedirectUri} className={"action-link"}>
            <Reply/>
            <span>reset redirect uri</span>
          </a>
        </FormInputField>
        <FormInputField name="authCodeParameters.queryParameters"
                        label="Additional URL Query"
                        value={authCodeParameters.queryParameters}
                        placeholder="add an optional query string that is appended to the request URL"
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}/>
        <Form.Group as={Row}>
          <Col sm={2}>
            Proof Key for Code Exchange (RFC7636)
          </Col>
          <Col>
            <FormCheck type="switch"
                       id="pkce.use"
                       name="pkce.use"
                       key={"pkce.use-" + this.state.pkce?.use}
                       checked={this.state.pkce?.use}
                       className={"d-inline-block"}
                       onChange={e =>
                       {
                         let pkce = this.state.pkce || {}
                         this.props.handleChange(e.target.name, e.target.checked)
                         lodash.set(pkce, "use", e.target.checked);
                         this.setState({pkce: pkce})
                       }}/>
            {
              this.state.pkce?.use &&
              <>
                <FormInputField name="pkce.codeVerifier"
                                label="Code Verifier"
                                placeholder="Optional. Will be auto-generated if not entered manually"
                                value={this.state.pkce?.codeVerifier}
                                onChange={e =>
                                {
                                  let pkce = this.state.pkce || {}
                                  this.props.handleChange(e.target.name, e.target.value)
                                  lodash.set(pkce, "codeVerifier", e.target.value);
                                  this.setState({
                                    pkce: pkce,
                                    pkceCodeVerifierLength: e.target.value.length
                                  })
                                }}
                                onError={fieldname => this.props.onError(fieldname)}>
                  <FormText className={"text-secondary"}>Must have a minimum length of 43 characters </FormText>
                  <FormText className={"text-secondary"}>({this.state.pkceCodeVerifierLength || 0} / 43)</FormText>
                </FormInputField>
              </>
            }
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Col sm={{span: 10, offset: 2}}>
            <Button type="submit" onClick={this.loadAuthorizationRequestDetails}>
              <LoadingSpinner show={this.state.getAuthcode}/> Get Authorization Code
            </Button>
            {
              this.state.errorMessage &&
              <Alert variant={"danger"}>
                <small className={"error"}>
                  <GoFlame/> {this.state.errorMessage}
                </small>
              </Alert>
            }
          </Col>
        </Form.Group>
      </React.Fragment>
    );
  }
}

class ClientCredentialsGrantForm extends React.Component
{

  constructor(props)
  {
    super(props);

    let previousDpopKeyId = props.workflowDetails?.dpop?.keyId;
    let previousDpopSignatureAlgorithm = props.workflowDetails?.dpop?.signatureAlgorithm;
    let previousDpopNonce = props.workflowDetails?.dpop?.nonce;
    let previousDpopJti = props.workflowDetails?.dpop?.jti;
    let previousDpopHtm = props.workflowDetails?.dpop?.htm;
    let previousDpopHtu = props.workflowDetails?.dpop?.htu;

    this.state = {
      dpopKeyId: previousDpopKeyId || ((props.keyInfos || []).length > 0 ? props.keyInfos[0].alias : null),
      dpopSignatureAlgorithm: previousDpopSignatureAlgorithm ||
        ((props.appInfo.jwtInfo.signatureAlgorithms || []).length > 0
          ? props.appInfo.jwtInfo.signatureAlgorithms[0] : null),
      dpopNonce: previousDpopNonce || "",
      dpopJti: previousDpopJti || "",
      dpopHtm: previousDpopHtm || "",
      dpopHtu: previousDpopHtu || ""
    };
    this.setState = this.setState.bind(this);
    this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
    this.patchWorkflowSettings = this.patchWorkflowSettings.bind(this);
  }

  retrieveAccessTokenDetails(e)
  {
    e.preventDefault();
    this.patchWorkflowSettings();
    this.setState({accessingToken: true})
    let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

    let scope = new Optional(this.props.workflowDetails).map(w => w.clientCredentialsParameters)
      .map(c => c.scope)
      .orElse(undefined);
    let resource = {
      grantType: "client_credentials",
      openIdClientId: parseInt(this.props.client.id),
      scope: scope
    }

    let openIdClientId = this.props.workflowDetails.openIdClientId;
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

    let handleResponse = this.props.handleResponse;
    scimClient.createResource(resource).then(response =>
    {
      this.setState({accessingToken: false})
      if (response.success)
      {
        response.resource.then(resource =>
        {
          handleResponse(resource);
        })
      }
    });
  }

  patchWorkflowSettings()
  {
    let workflowSettingsClient = new CurrentWorkflowSettingsClient(this.setState);

    let openIdClientId = new Optional(this.props.workflowDetails).map(w => w.openIdClientId)
      .orElse(undefined);
    let scope = new Optional(this.props.workflowDetails).map(w => w.clientCredentialsParameters)
      .map(c => c.scope)
      .orElse(undefined);

    workflowSettingsClient.updateClientCredentialsSettings(openIdClientId, scope);
  }

  render()
  {
    let clientCredentialsParameters = this.props.workflowDetails.clientCredentialsParameters || {};

    let errors = this.state.errors || {};
    return (
      <React.Fragment>
        <FormInputField name="clientCredentialsParameters.scope"
                        label="Scope"
                        value={clientCredentialsParameters.scope || ""}
                        placeholder="The scope to access from the identity provider"
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}/>
        <Form.Group as={Row}>
          <Col sm={2}>
            DPoP Details
          </Col>
          <Col>
            <FormCheck type="switch"
                       id="dpop-switch"
                       className={"d-inline-block"}
                       label="Demonstrate Proof of Posession (DPoP)"
                       onChange={e =>
                       {
                         this.setState({useDpop: e.target.checked})
                       }}/>
            {
              this.state.useDpop &&
              <DpopDetails props={this.props} state={this.state} setState={this.setState}/>
            }
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Col sm={{span: 10, offset: 2}}>
            <Button id={"upload"} type="submit" onClick={this.retrieveAccessTokenDetails}>
              <LoadingSpinner show={this.state.accessingToken}/> Get Access Token
            </Button>
            <AlertListMessages variant={"danger"} icon={<GoFlame/>}
                               messages={errors.errorMessages || new Optional(errors.detail).map(d => [d])
                                 .orElse([])}/>
          </Col>
        </Form.Group>
      </React.Fragment>
    );
  }
}

class ResourceOwnerPasswordCredentialsForm extends React.Component
{

  constructor(props)
  {
    super(props);

    let previousDpopKeyId = props.workflowDetails?.dpop?.keyId;
    let previousDpopSignatureAlgorithm = props.workflowDetails?.dpop?.signatureAlgorithm;
    let previousDpopNonce = props.workflowDetails?.dpop?.nonce;
    let previousDpopJti = props.workflowDetails?.dpop?.jti;
    let previousDpopHtm = props.workflowDetails?.dpop?.htm;
    let previousDpopHtu = props.workflowDetails?.dpop?.htu;

    this.state = {
      dpopKeyId: previousDpopKeyId || ((props.keyInfos || []).length > 0 ? props.keyInfos[0].alias : null),
      dpopSignatureAlgorithm: previousDpopSignatureAlgorithm ||
        ((props.appInfo.jwtInfo.signatureAlgorithms || []).length > 0
          ? props.appInfo.jwtInfo.signatureAlgorithms[0] : null),
      dpopNonce: previousDpopNonce || "",
      dpopJti: previousDpopJti || "",
      dpopHtm: previousDpopHtm || "",
      dpopHtu: previousDpopHtu || ""
    }
    this.setState = this.setState.bind(this);
    this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
  }

  retrieveAccessTokenDetails(e)
  {
    e.preventDefault();
    this.patchWorkflowSettings();

    this.setState({accessingToken: true});
    let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

    let resourceOwnerPasswordParameters = this.props.workflowDetails.resourceOwnerPasswordParameters || {};
    let username = resourceOwnerPasswordParameters.username || "";
    let password = resourceOwnerPasswordParameters.password || "";
    let scope = resourceOwnerPasswordParameters.scope || "";

    let resource = {
      grantType: "password",
      openIdClientId: parseInt(this.props.client.id),
      username: username,
      password: password,
      scope: scope
    }

    let openIdClientId = this.props.workflowDetails.openIdClientId;
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

    let handleResponse = this.props.handleResponse;
    scimClient.createResource(resource).then(response =>
    {
      this.setState({accessingToken: false});
      if (response.success)
      {
        response.resource.then(resource =>
        {
          handleResponse(resource);
        })
      }
    });
  }

  patchWorkflowSettings()
  {
    let workflowSettingsClient = new CurrentWorkflowSettingsClient(this.setState);

    let openIdClientId = new Optional(this.props.workflowDetails).map(w => w.openIdClientId)
      .orElse(undefined);
    let resourceOwnerPasswordParameters = this.props.workflowDetails.resourceOwnerPasswordParameters || {};
    let username = resourceOwnerPasswordParameters.username || "";
    let password = resourceOwnerPasswordParameters.password || "";
    let scope = resourceOwnerPasswordParameters.scope || "";

    workflowSettingsClient.updateResourceOwnerPasswordCredentialsSettings(openIdClientId, username, password,
      scope);
  }

  render()
  {
    let resourceOwnerPasswordParameters = this.props.workflowDetails.resourceOwnerPasswordParameters || {};
    let username = resourceOwnerPasswordParameters.username || "";
    let password = resourceOwnerPasswordParameters.password || "";
    let scope = resourceOwnerPasswordParameters.scope || "";

    let errors = this.state.errors || {};
    return (
      <React.Fragment>
        <FormInputField name="resourceOwnerPasswordParameters.username"
                        label="Username"
                        value={username}
                        placeholder={"the username to authenticate"}
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}/>
        <FormInputField name="resourceOwnerPasswordParameters.password"
                        label="Password"
                        placeholder={"the users password"}
                        value={password}
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}/>
        <FormInputField name="resourceOwnerPasswordParameters.scope"
                        label="Scope"
                        value={scope}
                        placeholder="The scope to access from the identity provider"
                        onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                        onError={fieldname => this.props.onError(fieldname)}/>
        <Form.Group as={Row}>
          <Col sm={2}>
            DPoP Details
          </Col>
          <Col>
            <FormCheck type="switch"
                       id="dpop-switch"
                       className={"d-inline-block"}
                       label="Demonstrate Proof of Posession (DPoP)"
                       onChange={e =>
                       {
                         this.setState({useDpop: e.target.checked})
                       }}/>
            {
              this.state.useDpop &&
              <DpopDetails props={this.props} state={this.state} setState={this.setState}/>
            }
          </Col>
        </Form.Group>
        <Form.Group as={Row}>
          <Col sm={{span: 10, offset: 2}}>
            <Button id={"upload"} type="submit" onClick={this.retrieveAccessTokenDetails}>
              <LoadingSpinner show={this.state.accessingToken}/> Get Access Token
            </Button>
            <AlertListMessages variant={"danger"} icon={<GoFlame/>}
                               messages={errors.errorMessages || new Optional(errors.detail).map(d => [d])
                                 .orElse([])}/>
          </Col>
        </Form.Group>
      </React.Fragment>
    );
  }
}
