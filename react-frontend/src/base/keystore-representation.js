import React from "react";
import Form from "react-bootstrap/Form";
import {InfoCircle} from "react-bootstrap-icons";
import {Alert, Badge, Image} from "react-bootstrap";
import downloadIcon from "../media/secure-download-icon.png";
import CertificateList from "../base/certificate-list";
import {GoThumbsup} from "react-icons/go";

export default class KeystoreRepresentation extends React.Component
{

    constructor(props)
    {
        super(props);
        if (props.basePath === undefined || props.basePath === null)
        {
            throw new Error("prop 'basePath' is required");
        }
        let certificateAliases = props.certificateAliases === undefined ? [] : props.certificateAliases;
        this.state = {
            numberOfEntries: certificateAliases.length,
            certificateAliases: certificateAliases
        }
        this.onCertificateDelete = this.onCertificateDelete.bind(this);
    }

    onCertificateDelete(alias)
    {
        let certificateAliases = this.state.certificateAliases;
        const index = certificateAliases.indexOf(alias);
        if (index > -1)
        {
            certificateAliases.splice(index, 1);
        }
        this.setState({
            numberOfEntries: certificateAliases.length,
            certificateAliases: certificateAliases.sort(),
            aliasDeleted: alias
        })
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.certificateAliases.length < this.props.certificateAliases.length)
        {
            this.setState({
                numberOfEntries: this.props.certificateAliases.length,
                certificateAliases: this.props.certificateAliases,
                aliasDeleted: undefined
            })
        }
    }

    render()
    {
        return (
            <React.Fragment>
                <h2 id="application-certificate-info-header">
                    <p>Application {this.props.type} Infos</p>
                    <Badge className="download-keystore-icon">
                        <a id={"keystore-download-link"} href={this.props.basePath + "/download"}>
                            <Image src={downloadIcon} fluid />
                            <p>Download</p>
                        </a>
                    </Badge>
                </h2>
                <Alert id="card-list-infos-alert"
                       variant={"info"}
                       show={this.state.numberOfEntries !== null}>
                    <Form.Text>
                        <InfoCircle /> Application {this.props.type} contains "{this.state.numberOfEntries}" entries
                    </Form.Text>
                </Alert>
                <Alert id="card-list-deletion-success"
                       variant={"success"}
                       show={this.state.aliasDeleted !== undefined}>
                    <Form.Text>
                        <GoThumbsup /> Key entry for alias "{this.state.aliasDeleted}" was successfully deleted
                    </Form.Text>
                </Alert>
                <CertificateList certificateAliases={this.state.certificateAliases}
                                 basePath={this.props.basePath}
                                 onDeleteSuccess={this.onCertificateDelete} />
            </React.Fragment>
        )
    }
}