import React from "react";
import Form from "react-bootstrap/Form";
import {ErrorMessagesAlert, FormInputField, FormRadioSelection, LoadingSpinner} from "../base/form-base";
import * as lodash from "lodash";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import AuthorizationCodeGrantForm from "./authorization-code-grant-form";

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
        this.responseToken = new URLSearchParams(window.location.search).get("responseToken")
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
                    {/*{*/}
                    {/*    this.state.workflow.authenticationType === "client_credentials" &&*/}
                    {/*    <ClientCredentialsGrantForm isLoading={this.state.isLoading} />*/}
                    {/*}*/}
                    {/*{*/}
                    {/*    this.state.workflow.authenticationType === "password" &&*/}
                    {/*    <ResourceOwnerPasswordCredentialsForm username={this.state.workflow.username}*/}
                    {/*                                          password={this.state.workflow.password}*/}
                    {/*                                          isLoading={this.state.isLoading}*/}
                    {/*                                          handleChange={this.handleChange}*/}
                    {/*                                          onError={() =>*/}
                    {/*                                          {*/}
                    {/*                                          }} />*/}
                    {/*}*/}
                </Form>
            </React.Fragment>
        )
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
