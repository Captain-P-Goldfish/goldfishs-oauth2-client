import React from "react";
import Spinner from "react-bootstrap/Spinner";
import {Card, CardDeck, Image} from "react-bootstrap";
import Modal from "./modal";
import {TrashFill} from "react-bootstrap-icons";
import CertIcon from "../media/certificate.png";
import Button from "react-bootstrap/Button";
import ScimClient from "../services/scim-client";
import * as ScimConstants from "../scim-constants";


export class CertificateCardEntry extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {loaded: false};
        this.deleteEntry = this.deleteEntry.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.loadData = this.loadData.bind(this);
    }

    async deleteEntry()
    {
        this.setState({showSpinner: true});

        let scimClient = new ScimClient(this.props.basePath);
        let response = await scimClient.deleteResource(this.props.alias);

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

        let scimClient = new ScimClient(this.props.basePath);
        let response = await scimClient.getResource(this.props.alias);

        if (response.success)
        {
            response.resource.then(resource =>
            {
                let certInfo = resource[ScimConstants.CERT_URI];
                this.setState({
                    showSpinner: false,
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
        let spinner;
        if (this.state.showSpinner)
        {
            spinner = <span style={{marginRight: 5 + 'px'}}>
                          <Spinner animation="border" variant="warning" size="sm" role="status" />
                      </span>;
        }

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
                    {this.props.alias}
                    <div className="card-delete-icon">
                        {spinner}
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
                                {this.state.certInfo.sha256fingerprint}
                            </Card.Text>
                            <Card.Subtitle>Valid From</Card.Subtitle>
                            <Card.Text id={"valid-from-" + this.props.alias}>
                                {this.state.certInfo.validFrom}
                            </Card.Text>
                            <Card.Subtitle>Valid Until</Card.Subtitle>
                            <Card.Text id={"valid-until-" + this.props.alias}>
                                {this.state.certInfo.validTo}
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
                                                     basePath={props.basePath}
                                                     alias={certAlias}
                                                     onDeleteSuccess={props.onDeleteSuccess} />
                    })
                }
            </CardDeck>
        </React.Fragment>
    );
}