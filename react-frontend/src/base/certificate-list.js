import React from "react";
import Spinner from "react-bootstrap/Spinner";
import {Card, CardDeck, Image} from "react-bootstrap";
import Modal from "./modal";
import {TrashFill} from "react-bootstrap-icons";
import CertIcon from "../media/certificate.png";
import Button from "react-bootstrap/Button";


export class CertificateCardEntry extends React.Component {

    constructor(props) {
        super(props);
        this.state = {loaded: false};
        this.deleteEntry = this.deleteEntry.bind(this);
        this.showModal = this.showModal.bind(this);
        this.hideModal = this.hideModal.bind(this);
        this.loadData = this.loadData.bind(this);
    }

    deleteEntry() {
        this.setState({showSpinner: true});
        let deleteUrl = this.props.deleteUrl;
        fetch(deleteUrl, {
            method: "DELETE"
        })
            .then(response => {
                if (response.status === 204) {
                    if (this.props.onDeleteSuccess !== undefined) {
                        this.props.onDeleteSuccess(this.props.alias);
                    }
                }
            })
    }

    async loadData() {
        this.setState({showSpinner: true});
        let loadUrl = this.props.loadUrl;
        fetch(loadUrl)
            .then(response => response.json())
            .then(response => {
                this.setState({
                    showSpinner: false,
                    loaded: true,
                    certInfo: response
                });
            })
    }

    showModal() {
        this.setState({showModal: true})
    }

    hideModal() {
        this.setState({showModal: false})
    }

    render() {
        let spinner;
        if (this.state.showSpinner) {
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
                                {this.state.certInfo.validUntil}
                            </Card.Text>
                        </React.Fragment>
                    }
                </Card.Body>
            </Card>
        );
    }
}

export default function CertificateList(props) {

    return (
        <React.Fragment>
            <CardDeck id="keystore-certificate-entries">
                {
                    props.certificateAliases.map((certAlias) => {
                        return <CertificateCardEntry key={certAlias}
                                                     loadUrl={props.loadUrl + "?alias=" + encodeURI(certAlias)}
                                                     deleteUrl={props.deleteUrl + "?alias=" + encodeURI(certAlias)}
                                                     alias={certAlias}
                                                     onDeleteSuccess={props.onDeleteSuccess} />
                    })
                }
            </CardDeck>
        </React.Fragment>
    );
}