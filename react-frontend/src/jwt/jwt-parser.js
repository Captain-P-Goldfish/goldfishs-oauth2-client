import React, {createRef} from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {AlertListMessages, FormInputField, LoadingSpinner} from "../base/form-base";
import Button from "react-bootstrap/Button";
import {Optional} from "../services/utils";
import {Col, Container, Dropdown, DropdownButton, Row} from "react-bootstrap";
import {ExclamationTriangle} from "react-bootstrap-icons";
import {GoFlame} from "react-icons/go";

export default class JwtParser extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {
            isMounted: false,
            jwtBuilder: {id: "1", header: "", body: ""},
            selectedKey: "",
            currentJwt: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        };
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient("/scim/v2/JwtBuilder", this.setState);
        this.formReference = createRef();
        this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
        this.handleKeySelectionSelection = this.handleKeySelectionSelection.bind(this);
        this.parseJwt = this.parseJwt.bind(this);
        this.prettyPrintJwtJson = this.prettyPrintJwtJson.bind(this);

        this.scimComponentBasics = new ScimComponentBasics({
            scimClient: this.scimClient,
            formReference: this.formReference,
            getOriginalResource: () => this.state.jwtBuilder,
            getCurrentResource: () => this.state.jwtBuilder,
            setCurrentResource: resource =>
            {
            },
            setState: this.setState,
            onUpdateSuccess: this.onUpdateSuccess
        });
    }

    componentDidMount()
    {
        this.setState({isMounted: true});
    }

    async handleKeySelectionSelection(value)
    {
        await this.setState({selectedKey: value})
        let hiddenKeyIdInputField = document.getElementById("jwt-parser-key-id");
        hiddenKeyIdInputField.value = value;
    }

    onUpdateSuccess(resource)
    {
        let jwtBuilder = resource;
        jwtBuilder.header = JSON.stringify(JSON.parse(resource.header), undefined, 4);
        this.setState({jwtBuilder: jwtBuilder});
    }

    parseJwt()
    {
        if (this.state.isMounted === false)
        {
            return null;
        }
        let token = document.getElementById("jwt-to-parse").value;
        let jwtDetails = {};
        let parts = (token || "").split(".");
        jwtDetails.parts = parts;

        let jwtPartDecoder = function (tokenPart)
        {
            return decodeURIComponent(Buffer.from(tokenPart, "base64").toString());
        };

        if (parts.length === 5)
        {
            let base64Header = parts[0];
            let parseHeader = jwtPartDecoder(base64Header);
            jwtDetails.header = this.prettyPrintJwtJson(parseHeader);
            jwtDetails.infoMessages = ["JWE must be parsed at backend"];
            return jwtDetails;
        }

        if (parts.length !== 3)
        {
            jwtDetails.warnMessages = ["Not a valid JSON Web Token"];
            return jwtDetails;
        }

        let base64Header = parts[0];
        let base64Body = parts[1];

        let parseHeader = jwtPartDecoder(base64Header);
        let parsedBody = jwtPartDecoder(base64Body);
        jwtDetails.header = this.prettyPrintJwtJson(parseHeader);
        jwtDetails.body = this.prettyPrintJwtJson(parsedBody);
        return jwtDetails;
    }

    prettyPrintJwtJson(decodedTokenString)
    {
        try
        {
            let json = JSON.parse(decodedTokenString);
            return JSON.stringify(json, null, 2);
        } catch (ex)
        {
            return decodedTokenString;
        }
    }

    render()
    {
        let jwtDetails = this.parseJwt() || {};
        return (
            <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>

                <AlertListMessages icon={<GoFlame />} variant={"danger"}
                                   messages={(this.state.errorMessages || []).errors} />
                <AlertListMessages icon={<ExclamationTriangle />} variant={"warning"}
                                   messages={jwtDetails.warnMessages} />
                <AlertListMessages variant={"info"} messages={jwtDetails.infoMessages} />
                <Container>
                    <Row>
                        <Col sm={3}>
                            <Dropdown>
                                <DropdownButton id={"jwt-parser-aliases"}
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
                                <p>
                                    selected key:
                                    <span className={"code"}
                                          style={{marginLeft: "15px", color: "lightgreen"}}>
                                        {this.state.selectedKey}
                                    </span>
                                </p>
                            </Dropdown>

                            <Col>
                                <Button id={"parse-jwt"} type="submit">
                                    <LoadingSpinner show={this.state.isLoading} /> Parse JWT at backend
                                </Button>
                            </Col>
                        </Col>
                        <Col sm={4} className={"form-group"}>
                            <Form.Control id={"jwt-to-parse"}
                                          name={"jwt"}
                                          sm={12}
                                          as={"textarea"}
                                          value={this.state.currentJwt}
                                          onChange={e => this.setState({currentJwt: e.target.value})} />
                            {
                                <div id={"jwt-input"}
                                     className={"jwt-overlay"}>
                                    {
                                        (jwtDetails.parts || []).map((part, index) =>
                                        {
                                            return <span key={"jwt-part-" + index}
                                                         className={"jwt-part jwt-part-" + (index <= 4 ? index
                                                                                                       : "over")}>
                                                {
                                                    index > 0 &&
                                                    <span className={"jwt-dot-separator"}>
                                                        .<br />
                                                    </span>
                                                }
                                                {part}
                                            </span>
                                        })
                                    }
                                </div>
                            }
                        </Col>
                        <Col sm={5}>
                            <FormInputField id={"jwt-parser-key-id"}
                                            name="keyId"
                                            readOnly={true}
                                            type="hidden"
                                            value={this.state.selectedKey}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormInputField id={"jwt-parsed-header"}
                                            name="header"
                                            className={"jwt-part-0"}
                                            type="text"
                                            as="textarea"
                                            value={jwtDetails.header}
                                            onChange={() =>
                                            {/*do nothing*/
                                            }}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                            <FormInputField id={"jwt-parsed-body"}
                                            name="body"
                                            className={"jwt-part-1"}
                                            type="text"
                                            as="textarea"
                                            value={jwtDetails.body}
                                            onChange={() =>
                                            {/*do nothing*/
                                            }}
                                            onError={fieldName => this.scimClient.getErrors(this.state,
                                                fieldName)} />
                        </Col>
                    </Row>
                </Container>
            </Form>
        )
    }
}
