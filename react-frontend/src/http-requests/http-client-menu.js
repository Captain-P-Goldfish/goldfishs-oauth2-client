import React, {useState} from "react";
import {InnerMenubar} from "../services/inner-menubar";

export function HttpClientMenu(props)
{
  
  const [menuEntries, setMenuEntries] = useState(["keycloak", "scim-server"]);
  
  function selectMenuEntry(menuEntry)
  {
    props.selectMenuEntry(menuEntry);
  }
  
  function onMenuEntryAdd(menuEntry)
  {
  }
  
  function onMenuEntryUpdate(oldMenuEntry, newMenuEntry)
  {
  }
  
  function onMenuEntryDelete(menuEntry)
  {
  }
  
  return <InnerMenubar header={"Request Groups"}
                       entries={menuEntries}
                       onClick={selectMenuEntry}
                       onMenuEntryAdd={onMenuEntryAdd}
                       onMenuEntryUpdate={onMenuEntryUpdate}
                       onMenuEntryDelete={onMenuEntryDelete} />;
}


