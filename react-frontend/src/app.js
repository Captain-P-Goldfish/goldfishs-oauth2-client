import React from "react";
import logo from "./logo.svg";
import {Nav, Navbar} from "react-bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';
import KeystoreForm from "./configpages/keystore-form";


class Application extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            errors: undefined
        };
    }

    render() {

        return (
            <React.Fragment>
                <Navbar bg="navigation" variant="light" expand="md">
                    <Navbar.Brand href="#home">OAuth2 Rest Client</Navbar.Brand>
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="mr-auto" />
                        <img src={logo} className="react-logo" alt="logo" />
                    </Navbar.Collapse>
                </Navbar>

                <Navbar bg="navigation-left" className={"navbar-left"} expand="md" variant="dark">
                    <Nav className="flex-column">
                        <Nav.Link href="#keystores">Keystores</Nav.Link>
                        <Nav.Link href="#truststores">Truststores</Nav.Link>
                        <Nav.Link href="#proxies">Proxies</Nav.Link>
                    </Nav>
                </Navbar>

                <div className="main">
                    <KeystoreForm />
                </div>
            </React.Fragment>
        );
    }
}


export default Application;
