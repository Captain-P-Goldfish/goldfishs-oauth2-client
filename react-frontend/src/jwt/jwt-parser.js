import React, {createRef} from "react";
import './jwt-builder.css'
import ScimClient from "../scim/scim-client";
import ScimComponentBasics from "../scim/scim-component-basics";
import Form from "react-bootstrap/Form";
import {AlertListMessages, FormInputField, LoadingSpinner} from "../base/form-base";
import Button from "react-bootstrap/Button";
import {Optional} from "../services/utils";
import {Col, Container, Dropdown, DropdownButton, FormText, Row} from "react-bootstrap";
import {ExclamationTriangle} from "react-bootstrap-icons";
import {GoFlame} from "react-icons/go";
import {JWT_BUILDER_ENDPOINT} from "../scim/scim-constants";
import {Buffer} from 'buffer';
import {decodeBase64, isJson, prettyPrintJson} from "../base/utils";

export default class JwtParser extends React.Component
{

  constructor(props)
  {
    super(props);
    this.state = {
      isMounted: false,
      selectedKey: "",
      currentJwt: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    };
    this.setState = this.setState.bind(this);
    this.scimClient = new ScimClient(JWT_BUILDER_ENDPOINT, this.setState);
    this.formReference = createRef();
    this.onUpdateSuccess = this.onUpdateSuccess.bind(this);
    this.handleKeySelectionSelection = this.handleKeySelectionSelection.bind(this);
    this.parseJwt = this.parseJwt.bind(this);
    this.prettyPrintJwtJson = this.prettyPrintJwtJson.bind(this);

    this.scimComponentBasics = new ScimComponentBasics({
      scimClient: this.scimClient,
      formReference: this.formReference,
      getOriginalResource: () => this.state.jwtBuilder || {id: 1},
      getCurrentResource: () => this.state.jwtBuilder || {id: 1},
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

  getJwtParts()
  {
    if (this.state.isMounted === false)
    {
      return null;
    }
    let token = document.getElementById("jwt-to-parse").value;
    return (token || "").split(".");
  }

  parseJwt(parts)
  {
    if (this.state.isMounted === false)
    {
      return null;
    }
    let jwtDetails = {};
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
      return [jwtDetails, null];
    }

    if (parts.length !== 3)
    {
      jwtDetails.warnMessages = ["Not a valid JSON Web Token"];
      return [jwtDetails, {}];
    }

    let base64Header = parts[0];
    let base64Body = parts[1];

    let parseHeader = jwtPartDecoder(base64Header);
    let parsedBody = jwtPartDecoder(base64Body);
    jwtDetails.header = this.prettyPrintJwtJson(parseHeader);
    jwtDetails.body = this.prettyPrintJwtJson(parsedBody);


    let signatureAndDisclosures = this.getDisclosuresFromSignature(parts);

    return [jwtDetails, signatureAndDisclosures];
  }

  getDisclosuresFromSignature(parts)
  {
    if (parts.length !== 3)
    {
      return {
        signature: parts[2]
      };
    }
    let signature = parts[2];

    if (!signature.includes("~"))
    {
      return {
        signature: signature
      }
    }

    let signatureAndDisclosures = signature.split("~");
    if (signatureAndDisclosures.length === 1)
    {
      return {
        signature: signatureAndDisclosures[0]
      }
    }

    let disclosureArray = [];
    let disclosureEncodedArray = [];
    let keyBindingJwt = null;
    // we start at index 1 because index 0 is the signature of the SD-JWT. The disclosures come after
    for (let i = 1; i < signatureAndDisclosures.length; i++)
    {
      let encodedDisclosure = signatureAndDisclosures[i];
      disclosureEncodedArray.push(encodedDisclosure);

      let disclosure = decodeBase64(encodedDisclosure);
      let isDisclosureJson = isJson(disclosure);
      if (isDisclosureJson)
      {
        disclosure = prettyPrintJson(disclosure);
        disclosureArray.push(disclosure);
      } else
      {
        keyBindingJwt = encodedDisclosure;
      }
    }

    return {
      signature: signatureAndDisclosures[0],
      disclosuresEncoded: disclosureEncodedArray,
      disclosures: disclosureArray,
      keyBindingJwt: keyBindingJwt
    }
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
    let jwtParts = this.getJwtParts();
    let [jwtDetails, signatureAndDisclosures] = this.parseJwt(jwtParts) || [{}, {}];
    let isJws = new Optional(jwtParts).map(parts => parts.length === 3).orElse(false);
    let isJwe = new Optional(jwtParts).map(parts => parts.length === 5).orElse(false);

    let headerToDisplay = new Optional(this.state.jwtBuilder).map(details => details.header).orElse(
      jwtDetails.header);
    let bodyToDisplay = new Optional(this.state.jwtBuilder).map(details => details.body).orElse(jwtDetails.body);

    let errors = this.state.errors || {};
    return (
      <React.Fragment>
        <AlertListMessages variant={"danger"} icon={<GoFlame/>}
                           messages={errors.errorMessages || new Optional(errors.detail).map(d => [d]).orElse(
                             [])}/>
        <Form onSubmit={this.scimComponentBasics.onSubmit} ref={this.formReference}>

          <AlertListMessages icon={<GoFlame/>} variant={"danger"}
                             messages={(this.state.errorMessages || []).errors}/>
          <AlertListMessages icon={<ExclamationTriangle/>} variant={"warning"}
                             messages={jwtDetails.warnMessages}/>
          <AlertListMessages variant={"info"} messages={jwtDetails.infoMessages}/>
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
                          + "-"
                          + keyInfo.keyLength
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

                {
                  (isJwe === true || isJws === true) &&
                  <Button id={"parse-jwt"} type="submit" onClick={e =>
                  {
                    this.setState({jwtBuilder: undefined})
                  }
                  }>
                    <LoadingSpinner show={this.state.isLoading}/>
                    {
                      isJws === true &&
                      <span>Verify Signature</span>
                    }
                    {
                      isJwe === true &&
                      <span>Decrypt</span>
                    }
                  </Button>
                }
                {
                  this.state.jwtBuilder && isJws &&
                  <h5 style={{color: "lightgreen", marginTop: "50px"}}>Signature valid</h5>
                }
                {
                  this.state.jwtBuilder && isJwe &&
                  <h5 style={{color: "lightgreen", marginTop: "50px"}}>Successfully decrypted</h5>
                }
              </Col>
              <Col sm={4} className={"form-group"}>
                <Form.Control id={"jwt-to-parse"}
                              name={"jwt"}
                              sm={12}
                              as={"textarea"}
                              value={this.state.currentJwt}
                              onChange={e =>
                              {
                                this.setState({
                                  currentJwt: e.target.value,
                                  jwtBuilder: undefined
                                })
                              }}/>
                {
                  <div id={"jwt-input"}
                       className={"jwt-overlay"}>
                    {
                      (jwtDetails.parts || []).map((part, index) =>
                      {
                        if (jwtDetails.parts.length === 3 && index === 2)
                        {
                          return <span key={"jwt-part-" + index}
                                       className={"jwt-part jwt-part-" + (index <= 4 ? index
                                         : "over")}>
                                                {
                                                  index > 0 &&
                                                  <span className={"jwt-dot-separator"}>
                                                        .<br/>
                                                    </span>
                                                }
                            {signatureAndDisclosures.signature}
                        </span>
                        }
                        return <span key={"jwt-part-" + index}
                                     className={"jwt-part jwt-part-" + (index <= 4 ? index
                                       : "over")}>
                                                {
                                                  index > 0 &&
                                                  <span className={"jwt-dot-separator"}>
                                                        .<br/>
                                                    </span>
                                                }
                          {part}
                        </span>
                      })
                    }
                    {
                      jwtDetails.parts && jwtDetails.parts.length === 3 && signatureAndDisclosures.disclosuresEncoded &&
                      signatureAndDisclosures.disclosuresEncoded.length > 0 &&
                      <>
                        <br/>
                        <FormText>Disclosures</FormText>
                        <br/>
                        {
                          signatureAndDisclosures.disclosuresEncoded.map((disclosure, index) =>
                          {
                            if (signatureAndDisclosures.keyBindingJwt
                              && index === signatureAndDisclosures.disclosuresEncoded.length - 1)
                            {
                              return <span key={"disclosure-paragraph-" + index} className={"text-black"}>
                                <span className={"jwt-dot-separator"}>
                                  <br/>~<br/>
                                </span>
                                  {disclosure}
                              </span>
                            }
                            return <span key={"disclosure-paragraph-" + index}
                                         className={"disclosure disclosure-" + (index <= 4 ? index
                                           : "over")}>
                              <span className={"jwt-dot-separator"}>
                                <br/>~<br/>
                              </span>
                              {disclosure}
                            </span>
                          })
                        }
                      </>
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
                                  fieldName)}/>
                <FormInputField id={"jwt-parsed-header"}
                                name="header"
                                className={"jwt-part-0"}
                                type="text"
                                as="textarea"
                                value={headerToDisplay}
                                onChange={() =>
                                {/*do nothing*/
                                }}
                                onError={fieldName => this.scimClient.getErrors(this.state,
                                  fieldName)}/>
                <FormInputField id={"jwt-parsed-body"}
                                name="body"
                                className={"jwt-part-1"}
                                type="text"
                                as="textarea"
                                value={bodyToDisplay}
                                onChange={() =>
                                {/*do nothing*/
                                }}
                                onError={fieldName => this.scimClient.getErrors(this.state,
                                  fieldName)}/>
                {
                  signatureAndDisclosures && signatureAndDisclosures.disclosures &&
                  <>
                    {
                      signatureAndDisclosures.disclosures.map((disclosure, index) =>
                      {
                        return <div className={"plain-disclosure"}>
                          <FormText key={"jwt-disclosure-text-" + index}>
                            {"Disclosure " + index}
                          </FormText>
                          <Form.Control id={"jwt-disclosure-" + index}
                                        key={"jwt-disclosure-" + index}
                                        name={"jwt-disclosure-" + index}
                                        type="text"
                                        as="textarea"
                                        value={disclosure}
                          />
                        </div>
                      })
                    }
                  </>
                }
                {
                  signatureAndDisclosures && signatureAndDisclosures.keyBindingJwt &&
                  <>
                    <FormText className={"text-secondary"}>KB-JWT</FormText>
                    <FormInputField id={"sd-key-binding-jwt"}
                                    name={"sd-key-binding-jwt"}
                      // className={"jwt-part-1"}
                                    type="text"
                                    as="textarea"
                                    value={signatureAndDisclosures.keyBindingJwt}
                                    onChange={() =>
                                    {/*do nothing*/
                                    }}
                                    onError={() =>
                                    {/* do nothing */
                                    }}/>
                  </>
                }
              </Col>
            </Row>
          </Container>
        </Form>
      </React.Fragment>
    )
  }
}
