/**
 * this method expects a string that contains headers from a textfield and will parse the values into a JSON object
 * @param headerText a string value from a text field that expects to have an HTTP-heaer in each line
 */
import {Optional} from "../services/utils";

export function toHeadersObject(headerText)
{
    if (new Optional(headerText).isEmpty())
    {
        return {};
    }
    let lines = headerText.split('\n');
    for (let i = 0; i < lines.length; i++)
    {
        let line = lines[i];
        let keyValue = line.split(':');
        
    }
}
