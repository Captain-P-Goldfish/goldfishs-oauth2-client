import React from "react";
import Form from "react-bootstrap/Form";
import {ErrorMessagesAlert, FormInputField, FormRadioSelection, LoadingSpinner} from "../base/form-base";
import {Reply} from "react-bootstrap-icons";
import * as lodash from "lodash";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";

export default class OpenidClientWorkflow extends React.Component
{
    constructor(props)
    {
        super(props)
        this.state = {
            workflow: {
                authenticationType: "authorization_code",
                originalRedirectUri: "http://localhost:8080/authcode",
                redirectUri: "http://localhost:8080/authcode",
                username: "",
                password: ""
            },
            isLoading: false
        }
        this.resetRedirectUri = this.resetRedirectUri.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }


    async resetRedirectUri(e)
    {
        e.preventDefault();
        let workflow = this.state.workflow;
        lodash.set(workflow, "redirectUri", workflow.originalRedirectUri)
        await this.setState({workflow: workflow})
    }

    handleChange(fieldname, value)
    {
        let object = this.state.workflow;
        object = lodash.set(object, fieldname, value);
        this.setState({workflow: object})
    }

    render()
    {
        let authTypes = [
            {value: "authorization_code", display: "Authorization Code Grant/Flow"},
            {value: "client_credentials", display: "Client Credentials Grant"},
            {value: "password", display: "Resource Owner Password Credentials Grant"}
        ]

        return (
            <React.Fragment>
                <h2>OpenID Connect Workflow for {this.props.client.clientId}</h2>
                <ErrorMessagesAlert errors={this.state.errors} />

                <Form>
                    <FormRadioSelection name="authenticationType"
                                        label="AuthenticationType Type"
                                        displayType={"vertical"}
                                        selected={this.state.workflow.authenticationType}
                                        selections={authTypes}
                                        onChange={e => this.handleChange(e.target.name, e.target.value)}
                                        onError={() =>
                                        {
                                        }} />
                    {
                        this.state.workflow.authenticationType === "authorization_code" &&
                        <AuthorizationCodeGrantForm redirectUri={this.state.workflow.redirectUri}
                                                    isLoading={this.state.isLoading}
                                                    handleChange={this.handleChange}
                                                    resetRedirectUri={this.resetRedirectUri}
                                                    onError={() =>
                                                    {
                                                    }} />
                    }
                    {
                        this.state.workflow.authenticationType === "client_credentials" &&
                        <ClientCredentialsGrantForm isLoading={this.state.isLoading} />
                    }
                    {
                        this.state.workflow.authenticationType === "password" &&
                        <ResourceOwnerPasswordCredentialsForm username={this.state.workflow.username}
                                                              password={this.state.workflow.password}
                                                              isLoading={this.state.isLoading}
                                                              handleChange={this.handleChange}
                                                              onError={() =>
                                                              {
                                                              }} />
                    }
                </Form>
            </React.Fragment>
        )
    }
}

function AuthorizationCodeGrantForm(props)
{

    return (
        <React.Fragment>
            <FormInputField name="redirectUri"
                            label="Redirect URI"
                            value={props.redirectUri}
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)}>
                <a href={"#"} onClick={props.resetRedirectUri} className={"action-link"}>
                    <Reply /> <span>reset redirect uri</span>
                </a>
            </FormInputField>
            <FormInputField name="urlParameters"
                            label="URL Parameter"
                            placeholder="add an optional query string that is appended to the request URL"
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)} />
            <Form.Group as={Row}>
                <Col sm={{span: 10, offset: 2}}>
                    <Button id={"upload"} type="submit">
                        <LoadingSpinner show={props.isLoading} /> Get Authorization Code
                    </Button>
                </Col>
            </Form.Group>
        </React.Fragment>
    );
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
            <FormInputField name="username"
                            label="Username"
                            value={props.username}
                            onChange={e => props.handleChange(e.target.name, e.target.value)}
                            onError={fieldname => props.onError(fieldname)} />
            <FormInputField name="password"
                            label="Password"
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