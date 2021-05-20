import React from "react";
import logo from "./logo.svg";
import {Nav, Navbar} from "react-bootstrap";
import KeystoreConfigForm from "./system/keystore-config-form";
import TruststoreConfigForm from "./system/truststore-config-form";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {LinkContainer} from 'react-router-bootstrap'
import SystemOverview from "./system/system-overview";


function Application(props)
{

    return (
        <React.Fragment>
            <Router>
                <Navbar bg="navigation" expand="md">
                    <Navbar.Brand href="#home">Captain Goldfish's Rest Client</Navbar.Brand>
                    <Navbar.Collapse id="basic-navbar-nav">
                        <Nav className="mr-auto" />
                        <img src={logo} className="react-logo" alt="logo" />
                    </Navbar.Collapse>
                </Navbar>

                <Navbar bg="navigation-left" className={"navbar-left"} expand="md" variant="dark">
                    <Navbar.Collapse>

                        <Nav className="flex-column">

                            <LinkContainer exact to="/">
                                <Nav.Link>Home</Nav.Link>
                            </LinkContainer>
                            <LinkContainer exact to="/system">
                                <Nav.Link>System</Nav.Link>
                            </LinkContainer>
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>

                <div className="main">
                    {/* A <Switch> looks through its children <Route>s and
                     renders the first one that matches the current URL. */}
                    <Switch>
                        <Route path="/system">
                            <SystemOverview />
                        </Route>
                        <Route path="/keystore">
                            <KeystoreConfigForm />
                        </Route>
                        <Route path="/truststore">
                            <TruststoreConfigForm />
                        </Route>
                        <Route path="/">
                            <h2>Welcome</h2>
                        </Route>
                    </Switch>
                </div>
            </Router>
        </React.Fragment>
    );
}


export default Application;
