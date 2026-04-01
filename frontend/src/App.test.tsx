import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AppShell } from "./App";

describe("AppShell", () => {
  it("renders primary navigation links", () => {
    render(
      <MemoryRouter
        initialEntries={["/users/1"]}
        future={{
          v7_startTransition: true,
          v7_relativeSplatPath: true,
        }}
      >
        <AppShell />
      </MemoryRouter>,
    );

    expect(screen.getByRole("navigation", { name: "Primary" })).toBeInTheDocument();
    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.getByText("Transactions")).toBeInTheDocument();
  });
});
