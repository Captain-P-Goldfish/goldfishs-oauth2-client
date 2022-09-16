import React, {createRef} from "react";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import {ErrorMessagesAlert, FormFileField, FormInputField, FormSelectField, LoadingSpinner} from "../../base/form-base";
import {downloadBase64Data, Optional} from "../../services/utils";
import ScimClient from "../../scim/scim-client";
import {Alert, Badge, Card, CardGroup, Image} from "react-bootstrap";
import downloadIcon from "../../media/secure-download-icon.png";
import {InfoCircle} from "react-bootstrap-icons";
import {GoThumbsup} from "react-icons/go";
import {CertificateCardEntry} from "../../base/certificate-list";
import {KEYSTORE_ENDPOINT} from "../../scim/scim-constants";


export default class ApplicationKeystore extends React.Component
{

    constructor(props)
    {
        super(props);
        this.scimResourcePath = "/scim/v2/Keystore";
        this.state = {};
        this.setAliasSelectionResponse = this.setAliasSelectionResponse.bind(this);
        this.onAliasSelectionSuccess = this.onAliasSelectionSuccess.bind(this);
    }

    setAliasSelectionResponse(resource)
    {
        let copiedResource = JSON.parse(JSON.stringify(resource));
        this.setState({aliasSelectionResponse: copiedResource})
    }

    onAliasSelectionSuccess(resource)
    {
        this.setState({newKeyInfo: {value: resource.keyInfos[0]}})
    }

    render()
    {
        return (
            <React.Fragment>
                <KeystoreUpload scimResourcePath={this.scimResourcePath}
                                setAliasSelectionResponse={this.setAliasSelectionResponse} />
                <AliasSelection scimResourcePath={this.scimResourcePath}
                                aliasSelectionResponse={this.state.aliasSelectionResponse}
                                onCreateSuccess={this.onAliasSelectionSuccess} />
                <KeystoreEntryList scimResourcePath={this.scimResourcePath}
                                   newKeyInfo={this.state.newKeyInfo} />
            </React.Fragment>
        )
    }
}

class KeystoreUpload extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            success: false,
            showSpinner: false
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.formReference = createRef();
        this.upload = this.upload.bind(this);
        this.handleCreateResponse = this.handleCreateResponse.bind(this);
    }

    async upload(e)
    {
        e.preventDefault();
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.createResource(resource);
        this.handleCreateResponse(response);
    }

    handleCreateResponse(response)
    {
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.props.setAliasSelectionResponse(resource);
            })
            this.setState({success: true});
        }
        else
        {
            this.setState({success: false});
        }
    }

    render()
    {
        return (
            <React.Fragment>
                <h2>Keystore Upload</h2>
                <Alert id={"uploadForm-alert-success"} variant={"success"} show={this.state.success}>
                    <Form.Text><GoThumbsup /> Keystore was successfully uploaded</Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"uploadForm"} onSubmit={this.upload} ref={this.formReference}>
                    <FormFileField name="fileUpload.keystoreFile"
                                   label="Keystore File"
                                   placeholder="Select a keystore file"
                                   onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="fileUpload.keystorePassword"
                                    label="Keystore Password"
                                    type="password"
                                    placeholder="Keystore Password"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"upload"} type="submit">
                                <LoadingSpinner show={this.state.isLoading} /> Upload
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class AliasSelection extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {success: false};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(props.scimResourcePath, this.setState);
        this.formReference = createRef();
        this.save = this.save.bind(this);
    }

    async save(e)
    {
        e.preventDefault();
        let resource = await this.scimClient.getResourceFromFormReference(this.formReference);
        let response = await this.scimClient.createResource(resource);
        if (response.success)
        {
            response.resource.then(resource =>
            {
                this.props.onCreateSuccess(resource);
                this.setState({newKeyInfo: resource.keyInfos[0]});
            })
        }
        else
        {
            this.setState({newKeyInfo: undefined});
        }
    }

    render()
    {
        let stateId = new Optional(this.props.aliasSelectionResponse).map(val => val.aliasSelection)
                                                                     .map(val => val.stateId)
                                                                     .orElse(0);
        let aliases = new Optional(this.props.aliasSelectionResponse).map(val => val.aliasSelection)
                                                                     .map(val => val.aliases)
                                                                     .orElse([]);
        let className = new Optional(this.props.aliasSelectionResponse).map(val => "").orElse("disabled")
        return (
            <React.Fragment>
                <h2>Alias Selection</h2>
                <Alert id={"aliasSelectionForm-alert-success"}
                       variant={"success"}
                       show={new Optional(this.state.newKeyInfo).isPresent()}>
                    <Form.Text><GoThumbsup /> Entry with alias
                                              '{new Optional(this.state.newKeyInfo).map(info => info.alias)
                                                                                   .orElse("")}'
                                              was successfully added</Form.Text>
                </Alert>
                <ErrorMessagesAlert errors={this.state.errors} />
                <Form id={"aliasSelectionForm"} onSubmit={this.save} ref={this.formReference} className={className}>
                    <FormInputField name="aliasSelection.stateId"
                                    type="hidden"
                                    value={stateId}
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormSelectField name="aliasSelection.aliases"
                                     label="Alias"
                                     options={aliases}
                                     onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="aliasSelection.aliasOverride"
                                    label="Alias Override"
                                    type="text"
                                    placeholder="Store under another alias"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <FormInputField name="aliasSelection.privateKeyPassword"
                                    label="Private Key Password"
                                    type="password"
                                    placeholder="Optional if password is identical to keystore password"
                                    onError={fieldName => this.scimClient.getErrors(this.state, fieldName)} />
                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button id={"save"} type="Save">
                                <LoadingSpinner show={this.state.isLoading} /> Save
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            </React.Fragment>
        )
    }
}

class KeystoreEntryList extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {keyInfos: []};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.onDeleteSuccess = this.onDeleteSuccess.bind(this);
        this.downloadKeystore = this.downloadKeystore.bind(this);
    }

    async componentDidMount()
    {
        let response = await this.scimClient.listResources();
        if (response.success)
        {
            response.resource.then(listResponse =>
            {
                this.setState({
                    keyInfos: new Optional(listResponse.Resources[0]).map(val => val.keyInfos)
                                                                     .orElse([])
                })
            })
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.newKeyInfo !== this.props.newKeyInfo &&
            new Optional(this.props.newKeyInfo).map(info => info.value).isPresent())
        {
            this.setState(
                {
                    keyInfos: [...this.state.keyInfos, this.props.newKeyInfo.value],
                    aliasDeleted: undefined
                });

        }
    }

    onDeleteSuccess(alias)
    {
        let keyInfos = this.state.keyInfos;

        let findIndexOf = function findWithAttr(array, attr, value)
        {
            for (var i = 0; i < array.length; i += 1)
            {
                if (array[i][attr] === value)
                {
                    return i;
                }
            }
            return -1;
        }

        const indexOfAlias = findIndexOf(keyInfos, "alias", alias)
        if (indexOfAlias > -1)
        {
            keyInfos.splice(indexOfAlias, 1);
        }
        this.setState({
            aliasDeleted: alias,
            keyInfos: keyInfos
        })
    }

    downloadKeystore(e)
    {
        e.preventDefault();
        this.setState({downloading: true})
        this.scimClient.getResource("1", KEYSTORE_ENDPOINT, {attributes: "applicationKeystore"}).then(response =>
        {
            this.setState({downloading: false})
            if (response.success)
            {
                response.resource.then(resource =>
                {
                    let base64ApplicationKeystore = resource.applicationKeystore;
                    downloadBase64Data(base64ApplicationKeystore, "application-keystore-pw-123456.p12", "p12")
                })
            }
        })
    }

    render()
    {
        return (
            <React.Fragment>
                <h2 id="application-certificate-info-header">
                    <p>Application Keystore Infos</p>
                    <Badge className="download-keystore-icon">
                        <a id={"keystore-download-link"} href={"/#"} onClick={this.downloadKeystore}>
                            <LoadingSpinner show={this.state.downloading || false} />
                            <Image src={downloadIcon} fluid />
                            <p>Download</p>
                        </a>
                    </Badge>
                </h2>
                <Alert id="card-list-infos-alert"
                       variant={"info"}>
                    <Form.Text>
                        <InfoCircle /> Application Keystore contains
                                       "{new Optional(this.state.keyInfos).map(val => val.length)
                                                                          .orElse(0)}"
                                       entries
                    </Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.aliasDeleted !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Key entry for alias "{this.state.aliasDeleted}" was successfully deleted
                    </Form.Text>
                </Alert>
                <CardGroup id="keystore-certificate-entries">
                    {
                        this.state.keyInfos.map(keyInfo =>
                        {
                            return <CertificateCardEntry key={keyInfo.alias}
                                                         scimResourcePath={this.props.scimResourcePath}
                                                         alias={keyInfo.alias}
                                                         keyInfo={keyInfo}
                                                         onDeleteSuccess={this.onDeleteSuccess} />
                        })
                    }
                </CardGroup>
            </React.Fragment>
        );
    }
}
