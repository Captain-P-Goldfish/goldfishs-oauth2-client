import React from "react";
import {Card, CardGroup, Image, Tooltip} from "react-bootstrap";
import OverlayTrigger from 'react-bootstrap/OverlayTrigger';
import Modal from "./modal";
import {AwardFill, KeyFill, TrashFill} from "react-bootstrap-icons";
import CertIcon from "../media/certificate.png";
import Button from "react-bootstrap/Button";
import ScimClient from "../scim/scim-client";
import * as ScimConstants from "../scim/scim-constants";
import {LoadingSpinner} from "./form-base";
import {Optional} from "../services/utils";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";


export class CertificateCardEntry extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {loaded: false};
        this.setState = this.setState.bind(this);
        this.scimClient = new ScimClient(this.props.scimResourcePath, this.setState);
        this.deleteEntry = this.deleteEntry.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.loadData = this.loadData.bind(this);
    }

    async deleteEntry()
    {
        let response = await this.scimClient.deleteResource(this.props.alias);

        if (response.success)
        {
            if (this.props.onDeleteSuccess !== undefined)
            {
                this.props.onDeleteSuccess(this.props.alias);
            }
        }
        else
        {
            // TODO
        }
    }

    async loadData()
    {
        this.setState({showSpinner: true});

        let response = await this.scimClient.getResource(this.props.alias);

        if (response.success)
        {
            response.resource.then(resource =>
            {
                let certInfo = resource[ScimConstants.CERT_URI];
                this.setState({
                    loaded: true,
                    cert: certInfo,
                    certInfo: certInfo.info
                });
            });
        }
        else
        {
            // TODO
        }
    }

    showModal()
    {
        this.setState({showModal: true})
    }

    hideModal()
    {
        this.setState({showModal: false})
    }

    render()
    {
        const certificateTooltip = (props) => (
            <Tooltip id="button-tooltip" {...props}>
                Certificate
            </Tooltip>
        );
        const privateKeyTooltip = (props) => (
            <Tooltip id="button-tooltip" {...props}>
                Private Key and Certificate
            </Tooltip>
        );

        return (
            <Card id={"alias-card-" + this.props.alias} key={this.props.alias}
                  border={"warning"} bg={"dark"} className={"alias-card"}>
                <Modal id={"delete-dialog-" + this.props.alias}
                       show={this.state.showModal}
                       variant="danger"
                       title={"Delete '" + this.props.alias + "'"}
                       message="Are you sure?"
                       submitButtonText="delete"
                       onSubmit={this.deleteEntry}
                       cancelButtonText="cancel"
                       onCancel={this.hideModal}>
                </Modal>
                <Card.Header id={"alias-name-" + this.props.alias}>
                    {this.props.alias} {new Optional(this.props.keyInfo).map(info =>
                    <React.Fragment>
                        <br />
                        (
                        <span className={"keyInfo"}>
                            {info.keyAlgorithm + ": "
                             + info.keyLength + "-bit "}
                            {
                                info.hasPrivateKey &&
                                <OverlayTrigger placement="right"
                                                delay={{show: 250, hide: 400}}
                                                overlay={privateKeyTooltip}>
                                    <KeyFill />
                                </OverlayTrigger>
                            }
                            {
                                !info.hasPrivateKey &&
                                <OverlayTrigger placement="right"
                                                delay={{show: 250, hide: 400}}
                                                overlay={certificateTooltip}>
                                    <AwardFill />
                                </OverlayTrigger>
                            }
                        </span>
                        )
                    </React.Fragment>
                )
                                                                        .orElse(null)}
                    <div className="card-control-icons">
                        <LoadingSpinner show={this.state.isLoading} />
                        <TrashFill id={"delete-icon-" + this.props.alias} onClick={this.showModal} />
                    </div>
                </Card.Header>
                <Card.Body>
                    {
                        this.state.loaded === false &&
                        <React.Fragment>
                            <div className={"load-certificate-icon-container"}>
                                <Image src={CertIcon} className={"load-certificate-icon"} fluid /> <br />
                                <Button id={"load-certificate-data-button-for-" + this.props.alias}
                                        className={"card-load-icon"}
                                        type={"button"}
                                        onClick={this.loadData}>
                                    Load Data
                                </Button>
                            </div>
                        </React.Fragment>
                    }
                    {
                        this.state.loaded === true &&
                        <React.Fragment>
                            <Card.Subtitle>Issuer</Card.Subtitle>
                            <Card.Text id={"issuer-dn-" + this.props.alias}>
                                {this.state.certInfo.issuerDn}
                            </Card.Text>
                            <Card.Subtitle>Subject</Card.Subtitle>
                            <Card.Text id={"subject-dn-" + this.props.alias}>
                                {this.state.certInfo.subjectDn}
                            </Card.Text>
                            <Card.Subtitle>SHA-256 Fingerprint</Card.Subtitle>
                            <Card.Text id={"sha-256-" + this.props.alias}>
                                {this.state.certInfo.sha256Fingerprint}
                            </Card.Text>
                            <Card.Subtitle>Valid From</Card.Subtitle>
                            <Card.Text id={"valid-from-" + this.props.alias}>
                                {new Date(this.state.certInfo.validFrom).toUTCString()}
                            </Card.Text>
                            <Card.Subtitle>Valid Until</Card.Subtitle>
                            <Card.Text id={"valid-until-" + this.props.alias}>
                                {new Date(this.state.certInfo.validTo).toUTCString()}
                            </Card.Text>
                        </React.Fragment>
                    }
                </Card.Body>
            </Card>
        );
    }
}

export default function CertificateList(props)
{

    return (
        <React.Fragment>
            <Row>
                {
                    props.certificateAliases !== undefined &&
                    props.certificateAliases.map((certAlias) =>
                    {
                        return <Col>
                            <CertificateCardEntry key={certAlias}
                                                     scimResourcePath={props.scimResourcePath}
                                                     alias={certAlias}
                                                           onDeleteSuccess={props.onDeleteSuccess} />
                        </Col>
                    })
                }
            </Row>
        </React.Fragment>
    );
}
