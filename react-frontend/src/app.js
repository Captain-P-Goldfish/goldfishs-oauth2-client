import React from "react";
import GoldfishLogo from "./media/goldfish-logo.png";
import logo from "./media/logo.svg";
import {Container, Image, Nav, Navbar} from "react-bootstrap";
import {BrowserRouter as Router, Link, Route, Routes} from "react-router-dom";
import SystemOverview from "./admin/system/system-overview";
import ScimClient from "./scim/scim-client";
import OpenidProvider from "./openid/openid-provider";
import JwtHandler from "./jwt/jwt-handler";
import {AlertListMessages} from "./base/form-base";
import {GoFlame} from "react-icons/go";
import {TokenCategoryList} from "./tokens/token-category";
import {APP_INFO_ENDPOINT, SERVICE_PROVIDER_CONFIG_ENDPOINT} from "./scim/scim-constants";
import {FileParser} from "./file-parser/file-parser";
import OpenidClientsRoute from "./openid/openid-clients";
import OpenidClientOverviewRoute from "./openid/openid-client-overview";
import CertIcon from "./media/certificate.png";


export const ApplicationInfoContext = React.createContext(null);
export const ScimServiceProviderContext = React.createContext(null);

class Application extends React.Component
{

  constructor(props)
  {
    super(props);
    this.state = {
      serviceProviderConfig: {
        bulk: {
          maxOperations: 5,
          maxPayloadSize: 2097152
        },
        filter: {
          maxResults: 5
        }
      }
    }
    this.setState = this.setState.bind(this);
  }

  async componentDidMount()
  {
    let scimClient = new ScimClient(SERVICE_PROVIDER_CONFIG_ENDPOINT, this.setState);
    scimClient.listResources().then(response =>
    {
      if (response.success)
      {
        response.resource.then(serviceProviderConfig =>
        {
          this.setState({serviceProviderConfig: serviceProviderConfig});
        })
      }
    })

    scimClient.getResource(null, APP_INFO_ENDPOINT).then(response =>
    {
      if (response.success)
      {
        response.resource.then(appInfo =>
        {
          this.setState({appInfo: appInfo});
        })
      }
    });
  }

  render()
  {
    return (
      <React.Fragment>
        <Router>
          <Navbar collapseOnSelect expand="lg" bg="navigation">
            <Container>
              <Image src={GoldfishLogo} width={"50px"} fluid/>
              <Navbar.Brand href="#home"><span className={"mobile-aware"}>Captain Goldfish's Rest Client</span></Navbar.Brand>
              <Navbar.Toggle aria-controls="responsive-navbar-nav"/>
              <Navbar.Collapse id="responsive-navbar-nav">
                <Nav className="me-auto">
                  <Nav.Link as={Link} to="/views/openIdProvider">OpenID</Nav.Link>
                  <Nav.Link as={Link} to="/views/jwts">JWTs</Nav.Link>
                  <Nav.Link as={Link} to="/views/tokenCategories">Storage</Nav.Link>
                  <Nav.Link as={Link} to="/views/fileParser">File Parser</Nav.Link>
                  <Nav.Link as={Link} to="/views/system">System</Nav.Link>
                </Nav>
              </Navbar.Collapse>
              <Image src={logo} className="react-logo" alt="logo"/>
            </Container>
          </Navbar>

          <div className="main">
            <AlertListMessages variant={"danger"}
                               icon={<GoFlame/>}
                               messages={(this.state.errors || {}).errorMessages}/>

            <ApplicationInfoContext.Provider value={this.state.appInfo}>
              <ScimServiceProviderContext.Provider value={this.state.serviceProviderConfig}>
                <Routes>
                  <Route path="/views/system" element={<SystemOverview/>}/>
                  <Route path={"/views/openIdProvider/:providerId/client/:clientId"}
                         element={<OpenidClientOverviewRoute/>}/>
                  <Route path={"/views/openIdProvider/:id/openIdClients"}
                         element={<OpenidClientsRoute serviceProviderConfig={this.state.serviceProviderConfig}/>}/>
                  <Route path="/views/openIdProvider"
                         element={<OpenidProvider serviceProviderConfig={this.state.serviceProviderConfig}/>} />
                  <Route path="/views/jwts" element={<JwtHandler/>}/>
                  <Route path="/views/tokenCategories" element={<TokenCategoryList/>}/>
                  <Route path="/views/fileParser" element={<FileParser/>}/>
                  <Route path="/views" element={<OpenidProvider serviceProviderConfig={this.state.serviceProviderConfig} />} />
                  <Route path="/" element={<OpenidProvider serviceProviderConfig={this.state.serviceProviderConfig} />} />
                </Routes>
              </ScimServiceProviderContext.Provider>
            </ApplicationInfoContext.Provider>
          </div>
        </Router>
      </React.Fragment>
    );
  }
}


export default Application;
