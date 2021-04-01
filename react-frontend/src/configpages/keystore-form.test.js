import React from 'react';
import {render, screen, waitFor} from '@testing-library/react';
import Keystore from "./keystore-form";

test('render Keystore and validate form', async () => {
    render(<Keystore myProp={"test"} />);
    const linkElement = screen.getByText(/Keystore Password/i);
    expect(linkElement).toBeInTheDocument();

    // const alertDiv = document.getElementById('alert');
    // expect(alertDiv).toBeInTheDocument();
    // expect(alertDiv).toBeVisible();
    //
    // alertDiv.dispatchEvent(new MouseEvent('click', {bubbles: true}));
    //
    // await waitFor(() => {
    //     expect(alertDiv).not.toBeInTheDocument();
    // });
});
