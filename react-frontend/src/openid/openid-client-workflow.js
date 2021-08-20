import React from "react";
import Form from "react-bootstrap/Form";
import {ErrorMessagesAlert, FormInputField, FormRadioSelection, LoadingSpinner} from "../base/form-base";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {Reply} from "react-bootstrap-icons";
import AuthorizationCodeGrantWorkflow from "./auth-code-grant/authorization-code-grant-workflow";
import * as lodash from "lodash";

export default class OpenidClientWorkflow extends React.Component
{

    constructor(props, context)
    {
        super(props, context)
        this.authCodeGrantType = "authorization_code";
        this.clientCredentialsGrantType = "client_credentials";
        this.resourceOwnerGrantType = "password";
        this.currentWorkflowSettingsUri = "urn:ietf:params:scim:schemas:captaingoldfish:2.0:CurrentWorkflowSettings";

        this.state = {
            authenticationType: this.authCodeGrantType,
            originalRedirectUri: props.originalRedirectUri,
            authCodeParameters: this.props.client[this.currentWorkflowSettingsUri].authCodeParameters,
            resourceOwnerPasswordParameters: this.props.client[this.currentWorkflowSettingsUri].resourceOwnerPasswordParameters,
            isLoading: false
        }
        this.resetRedirectUri = this.resetRedirectUri.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleAuthTypeFormResponse = this.handleAuthTypeFormResponse.bind(this);
    }

    async resetRedirectUri(e)
    {
        e.preventDefault();
        let wrapperObject = this.state;
        lodash.set(wrapperObject, "authCodeParameters.redirectUri", this.state.originalRedirectUri);
        this.setState(wrapperObject)
    }

    handleAuthTypeFormResponse(type, responseDetails)
    {
        let responseDetailsArray = (this.state[type] || []);
        responseDetailsArray.push(responseDetails);
        let wrapperObject = {};
        wrapperObject[type] = responseDetailsArray
        this.setState(wrapperObject);
    }

    handleChange(fieldname, value)
    {
        let wrapperObject = this.state;
        lodash.set(wrapperObject, fieldname, value);
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

                <Form>
                    <FormRadioSelection name="authenticationType"
                                        label="AuthenticationType Type"
                                        displayType={"vertical"}
                                        selected={this.state.authenticationType}
                                        selections={authTypes}
                                        onChange={e => this.handleChange(e.target.name, e.target.value)}
                                        onError={() =>
                                        {
                                        }} />
                    {
                        this.state.authenticationType === this.authCodeGrantType &&
                        <AuthorizationCodeGrantForm redirectUri={this.state.authCodeParameters.redirectUri}
                                                    queryParameters={this.state.authCodeParameters.queryParameters}
                                                    isLoading={this.state.isLoading}
                                                    handleChange={this.handleChange}
                                                    resetRedirectUri={this.resetRedirectUri}
                                                    handleResponse={details => this.handleAuthTypeFormResponse(
                                                        this.state.authenticationType, details)}
                                                    onError={() =>
                                                    {
                                                    }} />
                    }
                    {
                        this.state.authenticationType === this.clientCredentialsGrantType &&
                        <ClientCredentialsGrantForm isLoading={this.state.isLoading}
                                                    handleResponse={details => this.handleAuthTypeFormResponse(
                                                        this.state.authenticationType, details)} />
                    }
                    {
                        this.state.authenticationType === this.resourceOwnerGrantType &&
                        <ResourceOwnerPasswordCredentialsForm
                            username={this.state.resourceOwnerPasswordParameters.username}
                            password={this.state.resourceOwnerPasswordParameters.password}
                            isLoading={this.state.isLoading}
                            handleChange={this.handleChange}
                            handleResponse={details => this.handleAuthTypeFormResponse(
                                this.state.authenticationType, details)}
                            onError={() =>
                            {
                            }} />
                    }
                </Form>
                {
                    (this.state[this.authCodeGrantType] || []).map((authTypeDetails) =>
                    {
                        return <div key={this.authCodeGrantType + "-" + authTypeDetails.backendToken}>
                            <AuthorizationCodeGrantWorkflow requestDetails={authTypeDetails}
                                                            onRemove={() =>
                                                            {
                                                                let authCodeRequestDetails = this.state[this.authCodeGrantType];
                                                                let detailsIndex = authCodeRequestDetails.indexOf(
                                                                    authTypeDetails);
                                                                authCodeRequestDetails.splice(detailsIndex, 1);
                                                                let wrapperObject = {};
                                                                wrapperObject[this.authCodeGrantType] =
                                                                    authCodeRequestDetails;
                                                                this.setState(wrapperObject)
                                                            }} />
                        </div>
                    })
                }
            </React.Fragment>
        )
    }
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
        this.loadAuthorizationCode = this.loadAuthorizationCode.bind(this);
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

    async loadAuthorizationCode(e)
    {
        e.preventDefault();
        let details = await this.loadAuthorizationRequestDetails();
        this.props.handleResponse(details);
    }

    async loadAuthorizationRequestDetails()
    {
        return {
            backendToken: Math.random().toString(36).substring(2, 15),
            authCodeUrl: "http://localhost:8081/auth/realms/goldfish/protocol/openid-connect/auth?client_id=goldfish-rest-client&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A8180%2Fauthcode&state=bab36cc5-73a7-4db7-9a01-dca7a6981256"
        }
    }

    render()
    {
        return (
            <React.Fragment>
                <FormInputField name="authCodeParameters.redirectUri"
                                label="Redirect URI"
                                value={this.props.redirectUri}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)}>
                    <a href={"/#"} onClick={this.props.resetRedirectUri} className={"action-link"}>
                        <Reply /> <span>reset redirect uri</span>
                    </a>
                </FormInputField>
                <FormInputField name="authCodeParameters.queryParameters"
                                label="Additional URL Query"
                                value={this.props.queryParameters}
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
            </React.Fragment>
        );
    }
}

function ClientCredentialsGrantForm(props)
{

    return (
        <React.Fragment>
            <Form.Group as={Row}>
                <Col sm={{span: 10, offset: 2}}>
                    <Button id={"upload"} type="submit">
                        <LoadingSpinner show={props.isLoading} /> Get Access Token
                    </Button>
                </Col>
            </Form.Group>
        </React.Fragment>
    );
}

function ResourceOwnerPasswordCredentialsForm(props)
{

    return (
        <React.Fragment>
            <FormInputField name="resourceOwnerPasswordParameters.username"
                            label="Username"
                            value={props.username}
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)} />
            <FormInputField name="resourceOwnerPasswordParameters.password"
                            label="Password"
                            value={props.password}
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)} />
            <Form.Group as={Row}>
                <Col sm={{span: 10, offset: 2}}>
                    <Button id={"upload"} type="submit">
                        <LoadingSpinner show={props.isLoading} /> Get Access Token
                    </Button>
                </Col>
            </Form.Group>
        </React.Fragment>
    );
}
