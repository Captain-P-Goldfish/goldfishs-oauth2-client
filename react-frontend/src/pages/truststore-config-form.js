import React from "react";
import {FormFileField, FormInputField} from "../base/config-page-form";
import Form from "react-bootstrap/Form";
import {Alert} from "react-bootstrap";
import {GoFlame} from "react-icons/go";
import KeystoreRepresentation from "../base/keystore-representation";
import ScimConfigPageForm from "../base/scim-config-page-form";
import ScimClient from "../scim/scim-client";
import {Optional} from "../services/utils";

export default class TruststoreConfigForm extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            numberOfEntries: null,
            certificateAliases: [],
            duplicateAliases: [],
            duplicateCertificates: []
        };
        this.componentDidMount = this.componentDidMount.bind(this);
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
    }

    async componentDidMount()
    {
        let scimClient = new ScimClient("/scim/v2/Truststore");
        let response = await scimClient.listResources();

        if (response.success)
        {
            response.resource.then(resource =>
            {
                let appTruststore = resource.Resources[0];
                if (appTruststore === undefined)
                {
                    this.setState({
                        numberOfEntries: 0,
                        certificateAliases: []
                    });
                }
                else
                {
                    this.setState({
                        numberOfEntries: resource.totalResults,
                        certificateAliases: appTruststore.aliases === undefined
                                            ? [] : appTruststore.aliases
                    });
                }
            })
        }
        else
        {
            this.setState({
                numberOfEntries: 0,
                certificateAliases: []
            });
        }
    }

    handleUploadSuccess(status, response)
    {
        let truststoreUploadResponse = response.truststoreUploadResponse;
        let certificateUploadResponse = response.certificateUploadResponse;

        let addedAliases;
        let duplicateAliases;
        let duplicateCertificates;

        if (truststoreUploadResponse !== undefined)
        {
            addedAliases = truststoreUploadResponse.aliases;
            duplicateAliases = new Optional(truststoreUploadResponse.duplicateAliases).do(val => val.sort()).orElse([]);
            duplicateCertificates =
                new Optional(truststoreUploadResponse.duplicateCertificateAliases).do(val => val.sort()).orElse([])
        }
        else
        {
            addedAliases = [certificateUploadResponse.alias];
        }

        let certificateAliases = this.state.certificateAliases;
        if (addedAliases !== undefined)
        {
            certificateAliases = new Optional(certificateAliases).map(val => val.concat(addedAliases))
                                                                 .do(val => val.sort())
                                                                 .orElse([]);
        }

        this.setState({
            duplicateAliases: new Optional(duplicateAliases).orElse([]),
            duplicateCertificates: new Optional(duplicateCertificates).orElse([]),
            certificateAliases: new Optional(certificateAliases).orElse([])
        });
    }

    render()
    {
        return (
            <React.Fragment>
                <Alert id={"upload-form-alert-duplicate-aliases"} variant={"warning"}
                       show={this.state.duplicateAliases.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the alias is
                                    duplicated.<br />
                                    Number of not added aliases: {this.state.duplicateAliases.length} <br />
                                    [{this.state.duplicateAliases}]
                    </Form.Text>
                </Alert>
                <Alert id={"upoad-form-alert-duplicate-certificates"} variant={"warning"}
                       show={this.state.duplicateCertificates.length > 0}>
                    <Form.Text>
                        <GoFlame /> The following aliases could not be added because the certificate is already
                                    present: [{this.state.duplicateCertificates}]
                    </Form.Text>
                </Alert>

                <ScimConfigPageForm formId="truststoreUploadForm"
                                    header="Truststore Upload"
                                    httpMethod="POST"
                                    submitUrl="/scim/v2/Truststore"
                                    onSubmitSuccess={this.handleUploadSuccess}
                                    buttonId="truststoreUploadButton"
                                    buttonText="Upload"
                                    successMessage="Truststore was successfully merged into application keystore">
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="truststoreUpload.truststoreFile"
                                           label="Truststore File"
                                           placeholder="Select a truststore file"
                                           onChange={onChange}
                                           onError={onError}
                            />
                            <FormInputField name="truststoreUpload.truststorePassword"
                                            label="Truststore Password"
                                            type="password"
                                            placeholder="Truststore Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ScimConfigPageForm>
                <ScimConfigPageForm formId="certUploadForm"
                                    header="Certificate File Upload"
                                    httpMethod="POST"
                                    submitUrl="/scim/v2/Truststore"
                                    onSubmitSuccess={this.handleUploadSuccess}
                                    buttonId="certFileUploadButton"
                                    buttonText="Upload"
                                    successMessage="Certificate was successfully added to application keystore">
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="certificateUpload.certificateFile"
                                           label="Certificate File"
                                           placeholder="Select a certificate file"
                                           onChange={onChange}
                                           onError={onError} />
                            <FormInputField name="certificateUpload.alias"
                                            label="Alias"
                                            placeholder="Certificate Alias"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ScimConfigPageForm>

                <KeystoreRepresentation type={"Truststore"}
                                        basePath={"/scim/v2/Truststore"}
                                        certificateAliases={this.state.certificateAliases} />

            </React.Fragment>
        )
    }

}
