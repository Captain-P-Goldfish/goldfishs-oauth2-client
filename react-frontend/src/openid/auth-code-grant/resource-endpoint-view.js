import React, {useState} from "react";
import {Optional} from "../../services/utils";
import {HttpRequest} from "../../http-requests/http-client-requester";
import {scimHttpHeaderToString} from "../../scim/scim-constants";
import {AlertListMessages} from "../../base/form-base";
import {GoFlame} from "react-icons/go";
import {HttpResponse} from "../../http-requests/http-request-menu-bar";

export function ResourceEndpointDetailsView(props)
{

  const [responses, setResponses] = useState([]);
  const [errors, setErrors] = useState({});

  let accessTokenDetails = JSON.parse(props.accessTokenDetails.plainResponse);

  return <React.Fragment>
    <h5>Access Resource Endpoints</h5>
    <HttpRequest minHeight={"10vh"}
                 url={JSON.parse(props.metaData).userinfo_endpoint}
                 httpHeader={"Authorization: " + accessTokenDetails.token_type + " " +
                   accessTokenDetails.access_token +
                   (props.accessTokenDetails.resourceEndpointHeaders || []).map(header =>
                   {
                     return "\n" + header.name + ": " + header.value
                   })}
                 onSuccess={resource =>
                 {
                   let resources = [...responses];
                   resources.splice(0, 0, resource);
                   setResponses(resources);
                 }}
                 onError={errorResponse =>
                 {
                   setErrors(errorResponse);
                 }}/>
    {
      (responses || []).map((response, index) =>
      {
        return <HttpResponse key={index}
                             responseStatus={response.responseStatus}
                             httpHeader={scimHttpHeaderToString(response.responseHeaders)}
                             responseBody={response.responseBody}
                             onClose={() =>
                             {
                               let resources = [...responses];
                               let indexOf = resources.indexOf(response);
                               resources.splice(indexOf, 1);
                               setResponses(resources);
                             }}/>;
      })
    }
    <AlertListMessages variant={"danger"} icon={<GoFlame/>}
                       messages={errors.errorMessages || new Optional(errors.detail).map(d => [d])
                         .orElse(
                           [])}
                       onClose={() => setErrors(null)}/>
  </React.Fragment>;
}
