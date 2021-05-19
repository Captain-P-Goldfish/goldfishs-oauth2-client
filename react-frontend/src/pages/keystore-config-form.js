import React from "react";
import {FormFileField, FormInputField, FormSelectField} from "../base/config-page-form";
import KeystoreRepresentation from "../base/keystore-representation";
import ScimConfigPageForm from "../base/scim-config-page-form";
import * as ScimConstants from "../scim-constants";
import ScimClient from "../services/scim-client";

export default class KeystoreConfigForm extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            stateId: "",
            aliasOptions: [],
            numberOfEntries: 0,
            uploadFormDisabled: false,
            selectAliasFormDisabled: true,
            certificateAliases: []
        }
        this.handleUploadSuccess = this.handleUploadSuccess.bind(this);
        this.handleSelectionSuccess = this.handleSelectionSuccess.bind(this);
        this.onAliasDeleteSuccess = this.onAliasDeleteSuccess.bind(this);
    }

    async componentDidMount()
    {
        let scimClient = new ScimClient("/scim/v2/Keystore");
        let response = await scimClient.listResources();

        if (response.success)
        {
            response.resource.then(resource =>
            {
                let appKeystore = resource.Resources[0];
                if (appKeystore === undefined)
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
                        certificateAliases: appKeystore.aliases === undefined ? [] : appKeystore.aliases
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
        let aliasSelection = response.aliasSelection;
        if (aliasSelection === undefined)
        {
            aliasSelection = {};
        }
        this.setState({
            stateId: aliasSelection.stateId,
            aliasOptions: aliasSelection.aliases === undefined ? [] : aliasSelection.aliases,
            uploadFormDisabled: true,
            selectAliasFormDisabled: false,
            aliasDeleted: undefined
        });
    }

    handleSelectionSuccess(status, response)
    {
        let certificateAliases = [];
        if (this.state.certificateAliases !== undefined)
        {
            this.state.certificateAliases.forEach(certificateAlias => certificateAliases.push(certificateAlias));
        }
        let newCertAlias = response[ScimConstants.CERT_URI];
        certificateAliases.push(newCertAlias.alias);
        this.setState({
            numberOfEntries: this.state.numberOfEntries + 1,
            certificateAliases: certificateAliases.sort(),
            aliasDeleted: undefined
        });
    }

    onAliasDeleteSuccess(alias)
    {
        let certificateAliases = this.state.certificateAliases.filter(item => item !== alias);
        this.setState({
            numberOfEntries: this.state.numberOfEntries - 1,
            certificateAliases: certificateAliases.sort(),
            aliasDeleted: alias
        });
    }

    render()
    {
        return (
            <React.Fragment>
                <ScimConfigPageForm formId="uploadForm"
                                    header="Keystore Upload"
                                    httpMethod="POST"
                                    submitUrl="/scim/v2/Keystore"
                                    onSubmitSuccess={this.handleUploadSuccess}
                                    buttonId="uploadButton"
                                    buttonText="Upload"
                                    successMessage="Keystore was successfully uploaded"
                                    disabled={this.state.uploadFormDisabled}>
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormFileField name="fileUpload.keystoreFile"
                                           label="Keystore File"
                                           placeholder="Select a keystore file"
                                           onChange={onChange}
                                           onError={onError}
                            />
                            <FormInputField name="fileUpload.keystorePassword"
                                            label="Keystore Password"
                                            type="password"
                                            placeholder="Keystore Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ScimConfigPageForm>
                <ScimConfigPageForm formId="aliasSelectionForm"
                                    header="Alias Selection"
                                    httpMethod="POST"
                                    submitUrl="/scim/v2/Keystore"
                                    onSubmitSuccess={this.handleSelectionSuccess}
                                    buttonId="saveButton"
                                    buttonText="Save"
                                    successMessage="Key Entry was successfully added"
                                    disabled={this.state.selectAliasFormDisabled}>
                    {({onChange, onError}) => (
                        <React.Fragment>
                            <FormInputField name="aliasSelection.stateId"
                                            type="hidden"
                                            onChange={onChange}
                                            onError={onError}
                                            value={this.state.stateId} />
                            <FormSelectField name="aliasSelection.aliases" label="Alias"
                                             onChange={onChange}
                                             onError={onError}
                                             options={this.state.aliasOptions} />
                            <FormInputField name="aliasSelection.aliasOverride"
                                            label="Alias Override"
                                            type="text"
                                            placeholder="Optional value to override the alias on save"
                                            onChange={onChange}
                                            onError={onError} />
                            <FormInputField name="aliasSelection.privateKeyPassword"
                                            label="Private Key Password"
                                            type="password"
                                            placeholder="Private Key Password"
                                            onChange={onChange}
                                            onError={onError} />
                        </React.Fragment>
                    )}
                </ScimConfigPageForm>

                <KeystoreRepresentation type={"Keystore"}
                                        basePath={"/scim/v2/Keystore"}
                                        certificateAliases={this.state.certificateAliases} />

            </React.Fragment>
        );
    }
}
