import React, {useState} from "react";
import {Button, Col, Container, FormLabel, Row} from "react-bootstrap";
import Form from "react-bootstrap/Form";
import {downloadBase64Data} from "../services/utils";

export function FileParser(props)
{

  const [b64Data, setB64Data] = useState("");
  // drag state
  const [dragActive, setDragActive] = React.useState(false);

  // handle drag events
  const handleDrag = function (e)
  {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover")
    {
      setDragActive(true);
    } else if (e.type === "dragleave")
    {
      setDragActive(false);
    }
  };


  function downloadB64AsFile()
  {
    downloadBase64Data(b64Data, "file", "");
  }

  return <React.Fragment>
    <Container>
      <Row>
        <Col>
          <FormLabel>
            Enter a base64 encoded file and click on parse to file to download it as file
          </FormLabel>
          <Form.Control id={"file-b64-input"} as={"textarea"}
                        onDragEnter={handleDrag}
                        className={dragActive ? "drag-active" : ""}
                        onChange={e => setB64Data(e.target.value)}/>
          <Button onClick={downloadB64AsFile}>
            parse to file
          </Button>
        </Col>
      </Row>
    </Container>
  </React.Fragment>
}
