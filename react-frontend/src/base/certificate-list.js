import React from "react";
import {Card, CardDeck, Image} from "react-bootstrap";
import Modal from "./modal";
import {TrashFill} from "react-bootstrap-icons";
import CertIcon from "../media/certificate.png";
import Button from "react-bootstrap/Button";
import ScimClient from "../scim/scim-client";
import * as ScimConstants from "../scim-constants";
import {LoadingSpinner} from "./form-base";


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
        return (
            <Card id={"alias-card-" + this.props.alias} key={this.props.alias}
                  border={"warning"} bg={"dark"} className={"resource-card"}>
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
                    {this.props.alias}
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
                                <Image src={CertIcon} className={"load-certificate-icon"} fluid />
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
            <CardDeck id="keystore-certificate-entries">
                {
                    props.certificateAliases !== undefined &&
                    props.certificateAliases.map((certAlias) =>
                    {
                        return <CertificateCardEntry key={certAlias}
                                                     scimResourcePath={props.scimResourcePath}
                                                     alias={certAlias}
                                                     onDeleteSuccess={props.onDeleteSuccess} />
                    })
                }
            </CardDeck>
        </React.Fragment>
    );
}