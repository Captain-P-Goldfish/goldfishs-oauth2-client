import React, {createRef} from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {ErrorListItem, FormCheckbox, FormInputField, LoadingSpinner} from "../base/form-base";
import Button from "react-bootstrap/Button";
import {Optional} from "../services/utils";
import {Alert, Col, Container, Dropdown, DropdownButton, Row, Tooltip} from "react-bootstrap";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import * as lodash from "lodash";

export default class JwtBuilder extends React.Component
{
    constructor(props)
    {
        super(props);
        this.state = {
            jwtBuilder: {},
            header: {},
            body: {},
            addX5Sha256tHeader: false
        }
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient("/scim/v2/JwtBuilder", this.setState);
        this.formReference = createRef();
        this.onCreateSuccess = this.onCreateSuccess.bind(this);
        this.handleKeySelectionSelection = this.handleKeySelectionSelection.bind(this);
        this.handleSignatureAlgorithmSelection = this.handleSignatureAlgorithmSelection.bind(this);
        this.handleEncryptionAlgorithmSelection = this.handleEncryptionAlgorithmSelection.bind(this);
        this.handleContentEncryptionAlgorithmSelection = this.handleContentEncryptionAlgorithmSelection.bind(this);
        this.handleHeaderChange = this.handleHeaderChange.bind(this);
        this.handleBodyChange = this.handleBodyChange.bind(this);
        this.addJwtBody = this.addJwtBody.bind(this);
        this.addDefaultJwtAttributes = this.addDefaultJwtAttributes.bind(this);
        this.addKeyIdToHeader = this.addKeyIdToHeader.bind(this);
        this.handleAddSha256Thumbprint = this.handleAddSha256Thumbprint.bind(this);

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.props.jwtBuilder,
            getCurrentResource: () => this.state.jwtBuilder,
            setCurrentResource: resource =>
            {
            },
            setState: this.setState,
            onCreateSuccess: this.onCreateSuccess
        });
    }

    componentDidMount()
    {
        this.addHeader();
        this.addDefaultJwtAttributes();
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.jwtInfo !== this.props.jwtInfo && this.props.jwtInfo !== undefined)
        {
            this.handleSignatureAlgorithmSelection(this.props.jwtInfo.signatureAlgorithms[0])
            this.addHeader();
        }
    }

    onCreateSuccess(resource)
    {
        let jwtArea = document.getElementById("jwt");
        jwtArea.value = resource.jwt;

        this.setState({header: JSON.parse(resource.header)});
        this.addHeader();
    }

    async handleKeySelectionSelection(value)
    {
        await this.setState({selectedKey: value})
        let hiddenKeyIdInputField = document.getElementById("keyId");
        hiddenKeyIdInputField.value = value;
    }

    addKeyIdToHeader()
    {
        let header = this.state.header;
        header["kid"] = this.state.selectedKey
        this.setState({header: header})
        this.addHeader();
    }

    handleSignatureAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["alg"] = value
        header["enc"] = undefined
        this.setState({header: header})
        this.addHeader();
    }

    handleEncryptionAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["alg"] = value
        header["enc"] = new Optional(this.state.header.enc).orElse(new Optional(this.state.jwtInfo)
            .map(val => val.encryptionAlgorithms[0]).orElse(""));
        this.setState({header: header})
        this.addHeader();
    }

    handleContentEncryptionAlgorithmSelection(value)
    {
        let header = this.state.header;
        header["enc"] = value
        this.setState({header: header})
        this.addHeader();
    }

    handleHeaderChange(e)
    {
        try
        {
            let header = JSON.parse(e.target.value);
            this.setState({header: header})
        } catch (Exception)
        {
        }
    }

    handleBodyChange(e)
    {
        try
        {
            let body = JSON.parse(e.target.value);
            this.setState({body: body})
        } catch (Exception)
        {
        }
    }

    addHeader()
    {
        let headerArea = document.getElementById("jwt-builder-header");
        headerArea.value = JSON.stringify(this.state.header, undefined, 4);
    }

    async addDefaultJwtAttributes()
    {
        let body = {
            iss: new Optional(this.state.body.iss).orElse(""),
            aud: new Optional(this.state.body.aud).orElse(""),
            sub: new Optional(this.state.body.sub).orElse(""),
            iat: new Date().getTime(),
            exp: new Date().getTime() + (3600 * 24),
            jti: new Optional(this.state.body.jti).orElse("")
        }
        let mergedBody = lodash.merge(this.state.body, body)

        await this.setState({body: mergedBody});
        this.addJwtBody();
    }

    addJwtBody()
    {
        let bodyArea = document.getElementById("jwt-builder-body");
        bodyArea.value = JSON.stringify(this.state.body, undefined, 4);
    }

    handleAddSha256Thumbprint(event)
    {
        let isSelected = event.target.checked;
        this.setState({addX5Sha256tHeader: isSelected});
    }

    render()
    {
        let kidMatchesHeader = this.state.selectedKey === this.state.header.kid;

        return (
            <Container>
                <Row>
                    <Col sm={12}>
                        {
                            ((this.state.errors || {}).errorMessages || []).length > 0 &&
                            <Alert variant={"danger"}>
                                <ul className="error-list">
                                    {this.state.errors.errorMessages.map((message, index) =>
                                        <ErrorListItem key={"error-message-" + index} message={message} />)}
                                </ul>
                            </Alert>
                        }
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Dropdown>
                            <DropdownButton id={"jwt-builder-aliases"}
                                            title={"available keys"}
                                            onSelect={this.handleKeySelectionSelection}>
                                {
                                    new Optional(this.props.keyInfos).isPresent() &&
                                    this.props.keyInfos.map((keyInfo) =>
                                    {
                                        return <Dropdown.Item key={keyInfo.alias}
                                                              eventKey={keyInfo.alias}>{keyInfo.alias + " ("
                                                                                        + keyInfo.keyAlgorithm
                                                                                        + "-" + keyInfo.keyLength
                                                                                        + "-bit)"}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                            <p>selected key:

                                <OverlayTrigger
                                    placement={"top"}
                                    overlay={
                                        <Tooltip>
                                            Add as 'kid' to JOSE-header
                                        </Tooltip>
                                    }
                                >
                                <span className={"code"}
                                      style={{
                                          marginLeft: "15px",
                                          color: kidMatchesHeader ? "lightgreen" : "lightcoral",
                                          cursor: "pointer"
                                      }}
                                      onClick={this.addKeyIdToHeader}>
                                   {this.state.selectedKey}
                                </span>
                                </OverlayTrigger>
                            </p>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"signatureAlgorithms"}
                                            title={"signature algorithms"}
                                            onSelect={this.handleSignatureAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).map(val => val["signatureAlgorithms"]).isPresent()
                                    &&
                                    this.props.jwtInfo["signatureAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"keyWrapAlgorithms"}
                                            title={"key wrap algorithms"}
                                            onSelect={this.handleEncryptionAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).map(val => val["keyWrapAlgorithms"]).isPresent()
                                    &&
                                    this.props.jwtInfo["keyWrapAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Dropdown>
                            <DropdownButton id={"contentEncryptionAlgorithms"}
                                            title={"content encryption algorithms"}
                                            onSelect={this.handleContentEncryptionAlgorithmSelection}>
                                {
                                    new Optional(this.props.jwtInfo).isPresent() &&
                                    this.props.jwtInfo["encryptionAlgorithms"].map((value) =>
                                    {
                                        return <Dropdown.Item key={value}
                                                              eventKey={value}>{value}</Dropdown.Item>
                                    })
                                }
                            </DropdownButton>
                        </Dropdown>

                        <Button type={"button"} className={"functional-button"} onClick={this.addDefaultJwtAttributes}>
                            extend by JWT attributes
                        </Button>

                        <Form.Check onChange={this.handleAddSha256Thumbprint}
                                    className={"jwt sha-256-check"}
                                    label={"Add SHA-256 thumbprint to header"} />

                    </Col>
                    <Col sm={9}>
                        <Container>
                            <Row>
                                <Col>
                                    <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>
                                        <FormInputField name="keyId"
                                                        type="hidden"
                                                        value={this.state.selectedKey || ""}
                                                        onError={fieldName => this.scimClient.getErrors(this.state,
                                                            fieldName)} />
                                        <FormCheckbox name="addX5Sha256tHeader"
                                                      type="hidden"
                                                      readOnly={true}
                                                      checked={this.state.addX5Sha256tHeader}
                                                      onError={fieldName => this.scimClient.getErrors(this.state,
                                                          fieldName)} />
                                        <FormInputField id={"jwt-builder-header"}
                                                        name="header"
                                                        type="text"
                                                        as="textarea"
                                                        onChange={this.handleHeaderChange}
                                                        onError={fieldName => this.scimClient.getErrors(this.state,
                                                            fieldName)} />
                                        <FormInputField id={"jwt-builder-body"}
                                                        name="body"
                                                        type="text"
                                                        as="textarea"
                                                        onChange={this.handleBodyChange}
                                                        onError={fieldName => this.scimClient.getErrors(this.state,
                                                            fieldName)} />
                                    </Form>
                                </Col>
                                <Col sm={5}>
                                    <Form.Control id={"jwt"}
                                                  sm={5}
                                                  as={"textarea"}
                                                  onChange={this.handleBodyChange} />

                                </Col>
                            </Row>
                        </Container>
                        <Button id={"create-jwt"} type={"button"} onClick={this.scimComponentBasics.onSubmit}>
                            <LoadingSpinner show={this.state.isLoading} /> Create
                        </Button>
                    </Col>
                </Row>
            </Container>
        )
    }
}

