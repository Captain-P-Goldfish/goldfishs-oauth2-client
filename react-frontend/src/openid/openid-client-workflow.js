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
import {CURRENT_WORKFLOW_URI} from "../scim/scim-constants";

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
            workflowDetails: this.props.client[CURRENT_WORKFLOW_URI],
            isLoading: false
        }

        this.formReference = createRef();
        this.resetRedirectUri = this.resetRedirectUri.bind(this);
        this.handleNestedElementChange = this.handleNestedElementChange.bind(this);
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

    handleNestedElementChange(fieldname, value)
    {
        let wrapperObject = this.state.workflowDetails;
        lodash.set(wrapperObject, fieldname, value);
        this.setState({workflowDetails: wrapperObject})
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

                <Form ref={this.formReference}>
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
                            workflowDetails={this.state.workflowDetails}
                            isLoading={this.state.isLoading}
                            handleChange={this.handleNestedElementChange}
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
                        return <div key={this.authCodeGrantType + "-" + authTypeDetails.id}>
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
        const resourcePath = "/scim/v2/AuthCodeGrantRequest";
        let scimClient = new ScimClient(resourcePath, this.setState);
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
        return (
            <React.Fragment>
                <FormInputField name="authCodeParameters.redirectUri"
                                label="Redirect URI"
                                placeholder="The redirect uri that is added to the request parameters"
                                value={this.props.workflowDetails.authCodeParameters.redirectUri}
                                onChange={e => this.props.handleChange(e.target.name, e.target.value)}
                                onError={fieldname => this.props.onError(fieldname)}>
                    <a href={"/#"} onClick={this.props.resetRedirectUri} className={"action-link"}>
                        <Reply /> <span>reset redirect uri</span>
                    </a>
                </FormInputField>
                <FormInputField name="authCodeParameters.queryParameters"
                                label="Additional URL Query"
                                value={this.props.workflowDetails.authCodeParameters.queryParameters}
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
                            value={props.workflowDetails.resourceOwnerPasswordParameters.username}
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)} />
            <FormInputField name="resourceOwnerPasswordParameters.password"
                            label="Password"
                            value={props.workflowDetails.resourceOwnerPasswordParameters.password}
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
