import React, {createRef} from "react";
import Form from "react-bootstrap/Form";
import {ErrorMessagesAlert, FormInputField, FormRadioSelection, LoadingSpinner} from "../base/form-base";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {Reply} from "react-bootstrap-icons";
import AuthorizationCodeGrantWorkflow from "./auth-code-grant/authorization-code-grant-workflow";
import * as lodash from "lodash";
import ScimClient from "../scim/scim-client";
import {ACCESS_TOKEN_REQUEST_ENDPOINT, AUTH_CODE_GRANT_ENDPOINT, CURRENT_WORKFLOW_URI} from "../scim/scim-constants";
import AccessTokenView from "./auth-code-grant/access-token-view";

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
            originalRedirectUri: props.originalRedirectUri,
            workflowDetails: this.props.client[CURRENT_WORKFLOW_URI] || {},
            isLoading: false,
            responseDetails: []
        }

        this.formReference = createRef();
        this.resetRedirectUri = this.resetRedirectUri.bind(this);
        this.handleNestedElementChange = this.handleNestedElementChange.bind(this);
        this.handleGrantTypeResponseDetails = this.handleGrantTypeResponseDetails.bind(this);
        this.removeGrantTypeDetails = this.removeGrantTypeDetails.bind(this);
    }

    async resetRedirectUri(e)
    {
        e.preventDefault();
        let wrapperObject = this.state;
        lodash.set(wrapperObject, "workflowDetails.authCodeParameters.redirectUri", this.state.originalRedirectUri);
        this.setState(wrapperObject)
    }

    handleGrantTypeResponseDetails(type, responseDetails)
    {
        let responseDetailsArray = (this.state.responseDetails || []);
        responseDetails.grantType = type;
        responseDetailsArray.unshift(responseDetails);
        let wrapperObject = {};
        wrapperObject.responseDetails = responseDetailsArray
        this.setState(wrapperObject);
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
                <ErrorMessagesAlert errors={this.state.errors} />

                <Form ref={this.formReference} onSubmit={e => e.preventDefault()}>
                    <FormRadioSelection name="authenticationType"
                                        label="AuthenticationType Type"
                                        displayType={"vertical"}
                                        selected={this.state.authenticationType}
                                        selections={authTypes}
                                        onChange={e => this.setState({authenticationType: e.target.value})}
                                        onError={() =>
                                        {
                                        }} />
                    {
                        this.state.authenticationType === this.authCodeGrantType &&
                        <AuthorizationCodeGrantForm formReference={this.formReference}
                                                    workflowDetails={this.state.workflowDetails}
                                                    isLoading={this.state.isLoading}
                                                    handleChange={this.handleNestedElementChange}
                                                    resetRedirectUri={this.resetRedirectUri}
                                                    handleResponse={details => this.handleGrantTypeResponseDetails(
                                                        this.authCodeGrantType, details)}
                                                    onError={() =>
                                                    {
                                                    }} />
                    }
                    {
                        this.state.authenticationType === this.clientCredentialsGrantType &&
                        <ClientCredentialsGrantForm formReference={this.formReference}
                                                    client={this.props.client}
                                                    isLoading={this.state.isLoading}
                                                    handleResponse={details => this.handleGrantTypeResponseDetails(
                                                        this.clientCredentialsGrantType, details)} />
                    }
                    {
                        this.state.authenticationType === this.resourceOwnerGrantType &&
                        <ResourceOwnerPasswordCredentialsForm
                            client={this.props.client}
                            workflowDetails={this.state.workflowDetails}
                            isLoading={this.state.isLoading}
                            handleChange={this.handleNestedElementChange}
                            handleResponse={details => this.handleGrantTypeResponseDetails(
                                this.state.authenticationType, details)}
                            onError={() =>
                            {
                            }} />
                    }
                </Form>
                {
                    (this.state.responseDetails || []).map(responseDetails =>
                    {
                        return <ResponseDetailsView key={"response-details-" + responseDetails.id}
                                                    responseDetails={responseDetails}
                                                    client={this.props.client}
                                                    removeGrantTypeDetails={this.removeGrantTypeDetails} />
                    })
                }
            </React.Fragment>
        )
    }
}

function ResponseDetailsView(props)
{

    let authCodeGrantType = "authorization_code";
    let clientCredentialsGrantType = "client_credentials";
    let resourceOwnerGrantType = "password";

    let responseDetails = props.responseDetails;

    if (authCodeGrantType === responseDetails.grantType)
    {
        return <div key={authCodeGrantType + "-" + responseDetails.id}>
            <AuthorizationCodeGrantWorkflow client={props.client}
                                            requestDetails={responseDetails}
                                            onRemove={() => props.removeGrantTypeDetails(
                                                responseDetails)} />
        </div>
    }

    if (clientCredentialsGrantType === responseDetails.grantType)
    {
        return <div key={clientCredentialsGrantType + "-" + responseDetails.id}>
            <AccessTokenView header={"Client Credentials Grant"}
                             accessTokenDetails={responseDetails}
                             onRemove={() => props.removeGrantTypeDetails(
                                 responseDetails)} />
        </div>
    }

    return <div key={resourceOwnerGrantType + "-" + responseDetails.id}>
        <AccessTokenView header={"Resource Owner Password Credentials Grant"}
                         accessTokenDetails={responseDetails}
                         onRemove={() => props.removeGrantTypeDetails(
                             responseDetails)} />
    </div>
}

class AuthorizationCodeGrantForm extends React.Component
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
        this.loadAuthorizationRequestDetails = this.loadAuthorizationRequestDetails.bind(this);
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

    loadAuthorizationRequestDetails(e)
    {
        e.preventDefault();
        let scimClient = new ScimClient(AUTH_CODE_GRANT_ENDPOINT, this.setState);
        let resource = scimClient.getResourceFromFormReference(this.props.formReference);
        resource[CURRENT_WORKFLOW_URI] = this.props.workflowDetails;

        let handleResponse = this.props.handleResponse;
        scimClient.createResource(resource).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    handleResponse(resource);
                })
            }
        });
    }

    render()
    {
        let authCodeParameters = this.props.workflowDetails.authCodeParameters || {};

        return (
            <React.Fragment>
                <FormInputField name="authCodeParameters.redirectUri"
                                label="Redirect URI"
                                placeholder="The redirect uri that is added to the request parameters"
                                value={authCodeParameters.redirectUri}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)}>
                    <a href={"/#"} onClick={this.props.resetRedirectUri} className={"action-link"}>
                        <Reply /> <span>reset redirect uri</span>
                    </a>
                </FormInputField>
                <FormInputField name="authCodeParameters.queryParameters"
                                label="Additional URL Query"
                                value={authCodeParameters.queryParameters}
                                placeholder="add an optional query string that is appended to the request URL"
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)} />
                <Form.Group as={Row}>
                    <Col sm={{span: 10, offset: 2}}>
                        <Button type="submit" onClick={this.loadAuthorizationRequestDetails}>
                            <LoadingSpinner show={this.props.isLoading} /> Get Authorization Code
                        </Button>
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
        this.setState = this.setState.bind(this);
        this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
    }

    retrieveAccessTokenDetails(e)
    {
        e.preventDefault();
        let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

        let resource = {
            grantType: "client_credentials",
            openIdClientId: parseInt(this.props.client.id)
        }
        let handleResponse = this.props.handleResponse;
        scimClient.createResource(resource).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    handleResponse(resource);
                })
            }
        });
    }

    render()
    {
        return (
            <React.Fragment>
                <Form.Group as={Row}>
                    <Col sm={{span: 10, offset: 2}}>
                        <Button id={"upload"} type="submit" onClick={this.retrieveAccessTokenDetails}>
                            <LoadingSpinner show={this.props.isLoading} /> Get Access Token
                        </Button>
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
        this.setState = this.setState.bind(this);
        this.retrieveAccessTokenDetails = this.retrieveAccessTokenDetails.bind(this);
    }

    retrieveAccessTokenDetails(e)
    {
        e.preventDefault();
        let scimClient = new ScimClient(ACCESS_TOKEN_REQUEST_ENDPOINT, this.setState);

        let resourceOwnerPasswordParameters = this.props.workflowDetails.resourceOwnerPasswordParameters || {};
        let username = resourceOwnerPasswordParameters.username || "";
        let password = resourceOwnerPasswordParameters.password || "";

        let resource = {
            grantType: "password",
            openIdClientId: parseInt(this.props.client.id),
            username: username,
            password: password
        }

        let handleResponse = this.props.handleResponse;
        scimClient.createResource(resource).then(response =>
        {
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    handleResponse(resource);
                })
            }
        });
    }

    render()
    {
        let resourceOwnerPasswordParameters = this.props.workflowDetails.resourceOwnerPasswordParameters || {};
        let username = resourceOwnerPasswordParameters.username || "";
        let password = resourceOwnerPasswordParameters.password || "";
        return (
            <React.Fragment>
                <FormInputField name="resourceOwnerPasswordParameters.username"
                                label="Username"
                                value={username}
                                placeholder={"the username to authenticate"}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)} />
                <FormInputField name="resourceOwnerPasswordParameters.password"
                                label="Password"
                                placeholder={"the users password"}
                                value={password}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)} />
                <Form.Group as={Row}>
                    <Col sm={{span: 10, offset: 2}}>
                        <Button id={"upload"} type="submit" onClick={this.retrieveAccessTokenDetails}>
                            <LoadingSpinner show={this.props.isLoading} /> Get Access Token
                        </Button>
                    </Col>
                </Form.Group>
            </React.Fragment>
        );
    }
}
