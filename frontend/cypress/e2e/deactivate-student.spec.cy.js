// UC-STU-deactivate-student: warning and cancellation behavior on the student details form.
describe('Deactivate a student', () => {
  beforeEach(() => {
    cy.login('b.gates@abc.edu', '123456')
    cy.window().then((window) => {
      window.localStorage.setItem('settings', JSON.stringify({ defaultSectionId: 2 }))
    })
    cy.intercept('POST', '**/students/search*', {
      body: {
        flag: true,
        code: 200,
        message: 'Find students successfully',
        data: {
          content: [{ id: 4, firstName: 'John', lastName: 'Smith', email: 'j.smith@abc.edu', enabled: true }],
          number: 0,
          size: 50,
          totalElements: 1
        }
      }
    })
    cy.intercept('GET', '**/students/4', {
      body: {
        flag: true,
        code: 200,
        message: 'Find student successfully',
        data: { id: 4, username: 'j.smith@abc.edu', firstName: 'John', lastName: 'Smith', email: 'j.smith@abc.edu', enabled: true, teamName: 'Team1' }
      }
    })
    cy.visit('/students')
    cy.get('button[icon="Edit"]').click()
    cy.get('[data-cy=deactivate-student]').click()
  })

  it('names every required consequence and sends no request on Cancel (step 4, 4a)', () => {
    cy.intercept('PATCH', '**/students/4/deactivation').as('deactivate')
    cy.get('.el-message-box').should('contain', 'immediately removes access')
      .and('contain', 'assignments').and('contain', 'submitted work')
      .and('contain', 'Earlier-week evaluations').and('contain', 'submission obligations')
      .and('contain', 'reactivated').and('contain', 'not permanent deletion')
    cy.contains('button', 'Cancel').click()
    cy.get('@deactivate.all').should('have.length', 0)
  })

  it('sends the dedicated transition only after confirmation (step 5)', () => {
    cy.intercept('PATCH', '**/students/4/deactivation', {
      body: { flag: true, code: 200, message: 'Deactivate student successfully', data: {} }
    }).as('deactivate')
    cy.contains('button', 'Confirm deactivation').click()
    cy.wait('@deactivate').its('request.body').should('be.empty')
  })
})
