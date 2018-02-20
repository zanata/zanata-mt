import * as React from 'react';
import { Link } from 'react-router-dom'
import { auth } from '../../config'

interface NavBarProps {
  errorData?
}

const items = [
  {name: 'Home', url: '/'},
  {name: 'Info', url: '/app/info'}
]

const NavBar: React.StatelessComponent<NavBarProps> = ({
     errorData,
     ...props
   }) => {
  const path = window.location.pathname
  return (
    <nav className='navbar navbar-toggleable-md navbar-light bg-faded mr-auto'>
      <button className='navbar-toggler navbar-toggler-right' type='button'
        data-toggle='collapse' data-target='#navbar' aria-controls='navbar'
        aria-expanded='false' aria-label='Toggle navigation'>
        <span className='navbar-toggler-icon'></span>
      </button>
      <div className='collapse navbar-collapse' id='navbar'>
        <ul className='navbar-nav mr-auto mt-2 mt-md-0'>
          {items.map((item, itemId) => {
            const isSelected = item.url === path
            return <li className={isSelected ? 'nav-item active' : 'nav-item'}
              key={itemId}>
              <Link className='nav-link' to={item.url}>
                {item.name} {isSelected &&
              <span className='sr-only'>(current)</span>}
              </Link>
            </li>
          })}
        </ul>
        {auth.loggedIn
          ? <Link className='btn btn-warning' to='/app/Logout'>Logout</Link>
          : <Link className='btn btn-primary' to='/app/login'>Login</Link>
        }
        <ul className='navbar-nav'>
          <li className='nav-item'>
            <a className='nav-link' target='_blank' href='https://github.com/zanata/zanata-mt'>
              Github
            </a>
          </li>
          <li className='nav-item'>
            <a className='nav-link' target='_blank' href='http://zanata.org/zanata-mt/apidocs/'>
              Docs
            </a>
          </li>
        </ul>
      </div>
    </nav>
  )
}

export default NavBar
