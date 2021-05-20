import React from "react";
import logo from "./logo.svg";
import {Nav, Navbar} from "react-bootstrap";
import KeystoreConfigForm from "./pages/keystore-config-form";
import TruststoreConfigForm from "./pages/truststore-config-form";
import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import {LinkContainer} from 'react-router-bootstrap'
import ProxyConfigForm from "./pages/proxy-config-form";


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
                            <LinkContainer exact to="/keystore">
                                <Nav.Link>Keystore</Nav.Link>
                            </LinkContainer>
                            <LinkContainer exact to="/truststore">
                                <Nav.Link>Truststore</Nav.Link>
                            </LinkContainer>
                            <LinkContainer exact to="/proxy">
                                <Nav.Link>Proxy</Nav.Link>
                            </LinkContainer>
                            {/*<Nav.Link href="#proxies">Proxies</Nav.Link>*/}
                        </Nav>
                    </Navbar.Collapse>
                </Navbar>

                <div className="main">
                    {/* A <Switch> looks through its children <Route>s and
                     renders the first one that matches the current URL. */}
                    <Switch>
                        <Route path="/proxy">
                            <ProxyConfigForm />
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
